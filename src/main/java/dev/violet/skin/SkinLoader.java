package dev.violet.skin;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.class_1011;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

public final class SkinLoader {
   private static final int MAX_TEXTURE = 2048;

   private SkinLoader() {
   }

   public static SkinMesh load(Path file, int budget) throws IOException {
      String name = file.getFileName().toString().toLowerCase(Locale.ROOT);
      float[] hints = new float[]{0.0F / 0.0F, 0.0F / 0.0F};
      List<SkinLoader.Raw> raw;
      if (name.endsWith(".glb")) {
         raw = glb(Files.readAllBytes(file), file.getParent(), hints);
      } else if (name.endsWith(".gltf")) {
         raw = gltf(JsonParser.parseString(Files.readString(file)).getAsJsonObject(), null, file.getParent(), hints);
      } else if (name.endsWith(".obj")) {
         raw = obj(file);
      } else {
         if (!name.endsWith(".stl")) {
            throw new IOException("Unsupported format");
         }

         raw = stl(Files.readAllBytes(file));
      }

      return finish(raw, budget, hints);
   }

   public static SkinMesh loadGlb(byte[] bytes, int budget) throws IOException {
      float[] hints = new float[]{0.0F / 0.0F, 0.0F / 0.0F};
      return finish(glb(bytes, null, hints), budget, hints);
   }

   private static SkinMesh finish(List<SkinLoader.Raw> raw, int budget, float[] hints) throws IOException {
      if (raw.isEmpty()) {
         throw new IOException("The model has no triangles");
      } else {
         for (SkinLoader.Raw part : raw) {
            if (part.normals == null) {
               smoothNormals(part);
            }
         }

         raw = decimate(raw, Math.max(1000, budget));
         normalize(raw);
         List<SkinMesh.Part> parts = new ArrayList<>();

         for (SkinLoader.Raw partx : raw) {
            if (partx.triangles.length == 0) {
               if (partx.image != null) {
                  partx.image.close();
               }
            } else {
               parts.add(new SkinMesh.Part(partx.positions, partx.uvs, partx.normals, partx.colors, partx.triangles, partx.image));
            }
         }

         SkinMesh mesh = new SkinMesh(parts);
         mesh.neckHint = hints[0];
         mesh.headHint = hints[1];
         return mesh;
      }
   }

   private static List<SkinLoader.Raw> glb(byte[] bytes, Path folder, float[] hints) throws IOException {
      ByteBuffer b = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
      if (bytes.length >= 20 && b.getInt(0) == 1179937895) {
         int length = Math.min(b.getInt(8), bytes.length);
         int offset = 12;
         JsonObject json = null;
         ByteBuffer bin = null;

         while (offset + 8 <= length) {
            int chunk = b.getInt(offset);
            int type = b.getInt(offset + 4);
            int start = offset + 8;
            if (start + chunk > bytes.length) {
               break;
            }

            if (type == 1313821514) {
               json = JsonParser.parseString(new String(bytes, start, chunk, StandardCharsets.UTF_8)).getAsJsonObject();
            } else if (type == 5130562) {
               bin = ByteBuffer.wrap(bytes, start, chunk).slice().order(ByteOrder.LITTLE_ENDIAN);
            }

            offset = start + chunk;
         }

         if (json == null) {
            throw new IOException("GLB without JSON");
         } else {
            return gltf(json, bin, folder, hints);
         }
      } else {
         throw new IOException("Not a GLB file");
      }
   }

   private static void hints(JsonObject json, float[] hints) {
      try {
         JsonObject data = json.getAsJsonObject("asset").getAsJsonObject("extras").getAsJsonObject("violet");
         if (data.has("neck")) {
            hints[0] = data.get("neck").getAsFloat();
         }

         if (data.has("headStrength")) {
            hints[1] = data.get("headStrength").getAsFloat();
         }
      } catch (RuntimeException var3) {
      }
   }

   private static List<ByteBuffer> buffers(JsonObject json, ByteBuffer bin, Path folder) throws IOException {
      List<ByteBuffer> list = new ArrayList<>();
      JsonArray array = json.has("buffers") ? json.getAsJsonArray("buffers") : new JsonArray();

      for (int i = 0; i < array.size(); i++) {
         JsonObject buffer = array.get(i).getAsJsonObject();
         if (!buffer.has("uri")) {
            list.add(bin != null ? bin : ByteBuffer.allocate(0));
         } else {
            list.add(ByteBuffer.wrap(uri(buffer.get("uri").getAsString(), folder)).order(ByteOrder.LITTLE_ENDIAN));
         }
      }

      return list;
   }

   private static byte[] uri(String uri, Path folder) throws IOException {
      if (uri.startsWith("data:")) {
         return Base64.getDecoder().decode(uri.substring(uri.indexOf(44) + 1));
      } else if (folder == null) {
         throw new IOException("External file without folder: " + uri);
      } else {
         return Files.readAllBytes(folder.resolve(URLDecoder.decode(uri, StandardCharsets.UTF_8)));
      }
   }

   private static int componentSize(int type) {
      return switch (type) {
         case 5120, 5121 -> 1;
         case 5122, 5123 -> 2;
         default -> 4;
      };
   }

   private static int components(String type) {
      return switch (type) {
         case "SCALAR" -> 1;
         case "VEC2" -> 2;
         case "VEC3" -> 3;
         case "VEC4" -> 4;
         case "MAT4" -> 16;
         default -> 1;
      };
   }

   private static float[] floats(JsonObject json, List<ByteBuffer> buffers, int index) {
      JsonObject accessor = json.getAsJsonArray("accessors").get(index).getAsJsonObject();
      int count = accessor.get("count").getAsInt();
      int comps = components(accessor.get("type").getAsString());
      float[] out = new float[count * comps];
      if (!accessor.has("bufferView")) {
         return out;
      } else {
         int type = accessor.get("componentType").getAsInt();
         boolean normalized = accessor.has("normalized") && accessor.get("normalized").getAsBoolean();
         JsonObject view = json.getAsJsonArray("bufferViews").get(accessor.get("bufferView").getAsInt()).getAsJsonObject();
         ByteBuffer buf = buffers.get(view.get("buffer").getAsInt());
         int size = componentSize(type);
         int base = (view.has("byteOffset") ? view.get("byteOffset").getAsInt() : 0) + (accessor.has("byteOffset") ? accessor.get("byteOffset").getAsInt() : 0);
         int stride = view.has("byteStride") ? view.get("byteStride").getAsInt() : size * comps;

         for (int e = 0; e < count; e++) {
            for (int c = 0; c < comps; c++) {
               int p = base + e * stride + c * size;

               float value = switch (type) {
                  case 5120 -> normalized ? Math.max((float)buf.get(p) / 127.0F, -1.0F) : (float)buf.get(p);
                  case 5121 -> normalized ? (float)(buf.get(p) & 255) / 255.0F : (float)(buf.get(p) & 255);
                  case 5122 -> normalized ? Math.max((float)buf.getShort(p) / 32767.0F, -1.0F) : (float)buf.getShort(p);
                  case 5123 -> normalized ? (float)(buf.getShort(p) & '\uffff') / 65535.0F : (float)(buf.getShort(p) & '\uffff');
                  default -> (float)((long)buf.getInt(p) & 4294967295L);
                  case 5126 -> buf.getFloat(p);
               };
               out[e * comps + c] = value;
            }
         }

         return out;
      }
   }

   private static int[] ints(JsonObject json, List<ByteBuffer> buffers, int index) {
      JsonObject accessor = json.getAsJsonArray("accessors").get(index).getAsJsonObject();
      int count = accessor.get("count").getAsInt();
      int[] out = new int[count];
      if (!accessor.has("bufferView")) {
         return out;
      } else {
         int type = accessor.get("componentType").getAsInt();
         JsonObject view = json.getAsJsonArray("bufferViews").get(accessor.get("bufferView").getAsInt()).getAsJsonObject();
         ByteBuffer buf = buffers.get(view.get("buffer").getAsInt());
         int size = componentSize(type);
         int base = (view.has("byteOffset") ? view.get("byteOffset").getAsInt() : 0) + (accessor.has("byteOffset") ? accessor.get("byteOffset").getAsInt() : 0);
         int stride = view.has("byteStride") ? view.get("byteStride").getAsInt() : size;

         for (int e = 0; e < count; e++) {
            int p = base + e * stride;

            out[e] = switch (type) {
               case 5121 -> buf.get(p) & 255;
               case 5123 -> buf.getShort(p) & '\uffff';
               default -> buf.getInt(p);
            };
         }

         return out;
      }
   }

   private static Matrix4f local(JsonObject node) {
      Matrix4f m = new Matrix4f();
      if (!node.has("matrix")) {
         if (node.has("translation")) {
            JsonArray t = node.getAsJsonArray("translation");
            m.translate(t.get(0).getAsFloat(), t.get(1).getAsFloat(), t.get(2).getAsFloat());
         }

         if (node.has("rotation")) {
            JsonArray r = node.getAsJsonArray("rotation");
            m.rotate(new Quaternionf(r.get(0).getAsFloat(), r.get(1).getAsFloat(), r.get(2).getAsFloat(), r.get(3).getAsFloat()));
         }

         if (node.has("scale")) {
            JsonArray s = node.getAsJsonArray("scale");
            m.scale(s.get(0).getAsFloat(), s.get(1).getAsFloat(), s.get(2).getAsFloat());
         }

         return m;
      } else {
         JsonArray a = node.getAsJsonArray("matrix");
         float[] values = new float[16];

         for (int i = 0; i < 16; i++) {
            values[i] = a.get(i).getAsFloat();
         }

         m.set(values);
         return m;
      }
   }

   private static List<SkinLoader.Raw> gltf(JsonObject json, ByteBuffer bin, Path folder, float[] hints) throws IOException {
      hints(json, hints);
      List<ByteBuffer> buffers = buffers(json, bin, folder);
      Map<Integer, SkinLoader.Builder> builders = new LinkedHashMap<>();
      Map<Integer, class_1011> images = new HashMap<>();
      JsonArray nodes = json.has("nodes") ? json.getAsJsonArray("nodes") : new JsonArray();
      List<Integer> roots = new ArrayList<>();
      if (json.has("scenes") && json.getAsJsonArray("scenes").size() > 0) {
         int scene = json.has("scene") ? json.get("scene").getAsInt() : 0;
         JsonObject sceneObject = json.getAsJsonArray("scenes").get(scene).getAsJsonObject();
         if (sceneObject.has("nodes")) {
            for (JsonElement e : sceneObject.getAsJsonArray("nodes")) {
               roots.add(e.getAsInt());
            }
         }
      }

      if (roots.isEmpty()) {
         boolean[] child = new boolean[nodes.size()];

         for (JsonElement e : nodes) {
            JsonObject node = e.getAsJsonObject();
            if (node.has("children")) {
               for (JsonElement c : node.getAsJsonArray("children")) {
                  child[c.getAsInt()] = true;
               }
            }
         }

         for (int i = 0; i < nodes.size(); i++) {
            if (!child[i]) {
               roots.add(i);
            }
         }
      }

      if (nodes.isEmpty() && json.has("meshes")) {
         for (int m = 0; m < json.getAsJsonArray("meshes").size(); m++) {
            mesh(json, buffers, folder, m, new Matrix4f(), null, builders, images);
         }
      }

      Matrix4f[] worlds = new Matrix4f[nodes.size()];

      for (int root : roots) {
         worldOf(nodes, root, new Matrix4f(), worlds, 0);
      }

      for (int ix = 0; ix < nodes.size(); ix++) {
         if (worlds[ix] != null) {
            JsonObject node = nodes.get(ix).getAsJsonObject();
            if (node.has("mesh")) {
               Matrix4f[] joints = node.has("skin") ? joints(json, buffers, node.get("skin").getAsInt(), worlds) : null;
               mesh(json, buffers, folder, node.get("mesh").getAsInt(), worlds[ix], joints, builders, images);
            }
         }
      }

      List<SkinLoader.Raw> result = new ArrayList<>();

      for (SkinLoader.Builder builder : builders.values()) {
         result.add(toRaw(builder));
      }

      return result;
   }

   private static void worldOf(JsonArray nodes, int index, Matrix4f parent, Matrix4f[] worlds, int depth) {
      if (depth <= 128 && index >= 0 && index < nodes.size() && worlds[index] == null) {
         JsonObject node = nodes.get(index).getAsJsonObject();
         Matrix4f world = new Matrix4f(parent).mul(local(node));
         worlds[index] = world;
         if (node.has("children")) {
            for (JsonElement child : node.getAsJsonArray("children")) {
               worldOf(nodes, child.getAsInt(), world, worlds, depth + 1);
            }
         }
      }
   }

   private static Matrix4f[] joints(JsonObject json, List<ByteBuffer> buffers, int skinIndex, Matrix4f[] worlds) {
      if (json.has("skins") && skinIndex >= 0 && skinIndex < json.getAsJsonArray("skins").size()) {
         JsonObject skin = json.getAsJsonArray("skins").get(skinIndex).getAsJsonObject();
         if (!skin.has("joints")) {
            return null;
         } else {
            JsonArray list = skin.getAsJsonArray("joints");
            float[] inverse = skin.has("inverseBindMatrices") ? floats(json, buffers, skin.get("inverseBindMatrices").getAsInt()) : null;
            Matrix4f[] result = new Matrix4f[list.size()];

            for (int k = 0; k < list.size(); k++) {
               int joint = list.get(k).getAsInt();
               Matrix4f world = joint >= 0 && joint < worlds.length && worlds[joint] != null ? worlds[joint] : new Matrix4f();
               Matrix4f bind = new Matrix4f();
               if (inverse != null && inverse.length >= (k + 1) * 16) {
                  float[] values = new float[16];
                  System.arraycopy(inverse, k * 16, values, 0, 16);
                  bind.set(values);
               }

               result[k] = new Matrix4f(world).mul(bind);
            }

            return result;
         }
      } else {
         return null;
      }
   }

   private static JsonObject specGloss(JsonObject material) {
      if (material != null && material.has("extensions")) {
         JsonObject extensions = material.getAsJsonObject("extensions");
         return extensions.has("KHR_materials_pbrSpecularGlossiness") ? extensions.getAsJsonObject("KHR_materials_pbrSpecularGlossiness") : null;
      } else {
         return null;
      }
   }

   private static int imageOf(JsonObject json, JsonObject material) {
      if (material == null) {
         return -1;
      } else {
         JsonObject textureRef = null;
         if (material.has("pbrMetallicRoughness") && material.getAsJsonObject("pbrMetallicRoughness").has("baseColorTexture")) {
            textureRef = material.getAsJsonObject("pbrMetallicRoughness").getAsJsonObject("baseColorTexture");
         } else {
            JsonObject gloss = specGloss(material);
            if (gloss != null && gloss.has("diffuseTexture")) {
               textureRef = gloss.getAsJsonObject("diffuseTexture");
            }
         }

         if (textureRef != null && textureRef.has("index")) {
            int texture = textureRef.get("index").getAsInt();
            if (!json.has("textures")) {
               return -1;
            } else {
               JsonObject tex = json.getAsJsonArray("textures").get(texture).getAsJsonObject();
               if (tex.has("source")) {
                  return tex.get("source").getAsInt();
               } else {
                  if (tex.has("extensions")) {
                     for (Entry<String, JsonElement> ext : tex.getAsJsonObject("extensions").entrySet()) {
                        JsonObject value = ext.getValue().getAsJsonObject();
                        if (value.has("source")) {
                           return value.get("source").getAsInt();
                        }
                     }
                  }

                  return -1;
               }
            }
         } else {
            return -1;
         }
      }
   }

   private static class_1011 image(JsonObject json, List<ByteBuffer> buffers, Path folder, int index) throws IOException {
      JsonObject image = json.getAsJsonArray("images").get(index).getAsJsonObject();
      byte[] data;
      if (image.has("bufferView")) {
         JsonObject view = json.getAsJsonArray("bufferViews").get(image.get("bufferView").getAsInt()).getAsJsonObject();
         ByteBuffer buf = buffers.get(view.get("buffer").getAsInt());
         int offset = view.has("byteOffset") ? view.get("byteOffset").getAsInt() : 0;
         data = new byte[view.get("byteLength").getAsInt()];

         for (int i = 0; i < data.length; i++) {
            data[i] = buf.get(offset + i);
         }
      } else {
         if (!image.has("uri")) {
            return null;
         }

         data = uri(image.get("uri").getAsString(), folder);
      }

      return decode(data);
   }

   private static void mesh(
      JsonObject json,
      List<ByteBuffer> buffers,
      Path folder,
      int meshIndex,
      Matrix4f world,
      Matrix4f[] joints,
      Map<Integer, SkinLoader.Builder> builders,
      Map<Integer, class_1011> images
   ) throws IOException {
      JsonObject mesh = json.getAsJsonArray("meshes").get(meshIndex).getAsJsonObject();
      Matrix3f normalMatrix = new Matrix3f(world).invert().transpose();

      for (JsonElement element : mesh.getAsJsonArray("primitives")) {
         JsonObject primitive = element.getAsJsonObject();
         if (primitive.has("extensions")
            && (
               primitive.getAsJsonObject("extensions").has("KHR_draco_mesh_compression")
                  || primitive.getAsJsonObject("extensions").has("EXT_meshopt_compression")
            )) {
            throw new IOException("Compressed GLB (Draco) is not supported. Export it without compression.");
         }

         int mode = primitive.has("mode") ? primitive.get("mode").getAsInt() : 4;
         if (mode == 4 || mode == 5 || mode == 6) {
            JsonObject attributes = primitive.getAsJsonObject("attributes");
            if (attributes.has("POSITION")) {
               float[] pos = floats(json, buffers, attributes.get("POSITION").getAsInt());
               int count = pos.length / 3;
               float[] nrm = attributes.has("NORMAL") ? floats(json, buffers, attributes.get("NORMAL").getAsInt()) : null;
               float[] uv = attributes.has("TEXCOORD_0") ? floats(json, buffers, attributes.get("TEXCOORD_0").getAsInt()) : null;
               float[] col = null;
               int colComps = 0;
               if (attributes.has("COLOR_0")) {
                  int accessor = attributes.get("COLOR_0").getAsInt();
                  colComps = components(json.getAsJsonArray("accessors").get(accessor).getAsJsonObject().get("type").getAsString());
                  col = floats(json, buffers, accessor);
               }

               JsonObject material = primitive.has("material") && json.has("materials")
                  ? json.getAsJsonArray("materials").get(primitive.get("material").getAsInt()).getAsJsonObject()
                  : null;
               float[] factor = new float[]{1.0F, 1.0F, 1.0F, 1.0F};
               JsonArray factorArray = null;
               if (material != null && material.has("pbrMetallicRoughness") && material.getAsJsonObject("pbrMetallicRoughness").has("baseColorFactor")) {
                  factorArray = material.getAsJsonObject("pbrMetallicRoughness").getAsJsonArray("baseColorFactor");
               } else if (specGloss(material) != null && specGloss(material).has("diffuseFactor")) {
                  factorArray = specGloss(material).getAsJsonArray("diffuseFactor");
               }

               if (factorArray != null) {
                  for (int i = 0; i < 4 && i < factorArray.size(); i++) {
                     factor[i] = factorArray.get(i).getAsFloat();
                  }
               }

               float[] jointIndex = null;
               float[] jointWeight = null;
               if (joints != null && attributes.has("JOINTS_0") && attributes.has("WEIGHTS_0")) {
                  jointIndex = floats(json, buffers, attributes.get("JOINTS_0").getAsInt());
                  jointWeight = floats(json, buffers, attributes.get("WEIGHTS_0").getAsInt());
                  if (jointIndex.length < count * 4 || jointWeight.length < count * 4) {
                     jointWeight = null;
                     jointIndex = null;
                  }
               }

               Matrix4f skinned = new Matrix4f();
               Matrix3f skinnedNormal = new Matrix3f();
               int imageIndex = uv != null ? imageOf(json, material) : -1;
               SkinLoader.Builder builder = builders.get(imageIndex);
               if (builder == null) {
                  builder = new SkinLoader.Builder();
                  if (imageIndex >= 0) {
                     class_1011 img = images.get(imageIndex);
                     if (img == null && !images.containsKey(imageIndex)) {
                        img = image(json, buffers, folder, imageIndex);
                        images.put(imageIndex, img);
                     }

                     builder.image = img;
                  }

                  builders.put(imageIndex, builder);
               }

               int first = builder.vertices();
               Vector3f p = new Vector3f();
               Vector3f n = new Vector3f();

               for (int v = 0; v < count; v++) {
                  Matrix4f transform = world;
                  Matrix3f normalTransform = normalMatrix;
                  if (jointIndex != null) {
                     skinned.zero();
                     float total = 0.0F;

                     for (int k = 0; k < 4; k++) {
                        float w = jointWeight[v * 4 + k];
                        int j = (int)jointIndex[v * 4 + k];
                        if (!(w <= 0.0F) && j >= 0 && j < joints.length) {
                           Matrix4f m = joints[j];
                           skinned.m00(skinned.m00() + m.m00() * w)
                              .m01(skinned.m01() + m.m01() * w)
                              .m02(skinned.m02() + m.m02() * w)
                              .m03(skinned.m03() + m.m03() * w);
                           skinned.m10(skinned.m10() + m.m10() * w)
                              .m11(skinned.m11() + m.m11() * w)
                              .m12(skinned.m12() + m.m12() * w)
                              .m13(skinned.m13() + m.m13() * w);
                           skinned.m20(skinned.m20() + m.m20() * w)
                              .m21(skinned.m21() + m.m21() * w)
                              .m22(skinned.m22() + m.m22() * w)
                              .m23(skinned.m23() + m.m23() * w);
                           skinned.m30(skinned.m30() + m.m30() * w)
                              .m31(skinned.m31() + m.m31() * w)
                              .m32(skinned.m32() + m.m32() * w)
                              .m33(skinned.m33() + m.m33() * w);
                           total += w;
                        }
                     }

                     if (total > 1.0E-6F) {
                        transform = skinned;
                        normalTransform = skinnedNormal.set(skinned);
                     }
                  }

                  transform.transformPosition(pos[v * 3], pos[v * 3 + 1], pos[v * 3 + 2], p);
                  if (nrm != null) {
                     normalTransform.transform(nrm[v * 3], nrm[v * 3 + 1], nrm[v * 3 + 2], n);
                     if (n.lengthSquared() > 1.0E-12F) {
                        n.normalize();
                     }
                  } else {
                     n.set(0.0F, 1.0F, 0.0F);
                  }

                  float r = factor[0];
                  float g = factor[1];
                  float bl = factor[2];
                  float a = factor[3];
                  if (col != null) {
                     r *= col[v * colComps];
                     g *= col[v * colComps + 1];
                     bl *= col[v * colComps + 2];
                     if (colComps > 3) {
                        a *= col[v * colComps + 3];
                     }
                  }

                  int color = clamp(a) << 24 | clamp(r) << 16 | clamp(g) << 8 | clamp(bl);
                  builder.vertex(p.x, p.y, p.z, uv != null ? uv[v * 2] : 0.0F, uv != null ? uv[v * 2 + 1] : 0.0F, n.x, n.y, n.z, color);
               }

               int[] indices;
               if (primitive.has("indices")) {
                  indices = ints(json, buffers, primitive.get("indices").getAsInt());
               } else {
                  indices = new int[count];
                  int i = 0;

                  while (i < count) {
                     indices[i] = i++;
                  }
               }

               boolean flip = world.determinant() < 0.0F;
               if (mode == 4) {
                  for (int i = 0; i + 2 < indices.length; i += 3) {
                     triangle(builder, first, indices[i], indices[i + 1], indices[i + 2], flip, count);
                  }
               } else if (mode == 5) {
                  for (int i = 0; i + 2 < indices.length; i++) {
                     if ((i & 1) == 0) {
                        triangle(builder, first, indices[i], indices[i + 1], indices[i + 2], flip, count);
                     } else {
                        triangle(builder, first, indices[i + 1], indices[i], indices[i + 2], flip, count);
                     }
                  }
               } else {
                  for (int ix = 1; ix + 1 < indices.length; ix++) {
                     triangle(builder, first, indices[0], indices[ix], indices[ix + 1], flip, count);
                  }
               }

               if (nrm == null) {
                  builder.normals.replaceAll(v -> 0.0F / 0.0F);
               }
            }
         }
      }
   }

   private static void triangle(SkinLoader.Builder builder, int first, int a, int b, int c, boolean flip, int count) {
      if (a >= 0 && b >= 0 && c >= 0 && a < count && b < count && c < count) {
         builder.triangles.add(first + a);
         builder.triangles.add(first + (flip ? c : b));
         builder.triangles.add(first + (flip ? b : c));
      }
   }

   private static int clamp(float v) {
      return Math.max(0, Math.min(255, Math.round(v * 255.0F)));
   }

   private static SkinLoader.Raw toRaw(SkinLoader.Builder builder) {
      float[] positions = new float[builder.positions.size()];

      for (int i = 0; i < positions.length; i++) {
         positions[i] = builder.positions.get(i);
      }

      float[] uvs = new float[builder.uvs.size()];

      for (int i = 0; i < uvs.length; i++) {
         uvs[i] = builder.uvs.get(i);
      }

      boolean missing = false;
      float[] normals = new float[builder.normals.size()];

      for (int i = 0; i < normals.length; i++) {
         normals[i] = builder.normals.get(i);
         if (Float.isNaN(normals[i])) {
            missing = true;
         }
      }

      int[] colors = builder.colors.stream().mapToInt(Integer::intValue).toArray();
      int[] triangles = builder.triangles.stream().mapToInt(Integer::intValue).toArray();
      return new SkinLoader.Raw(positions, uvs, missing ? null : normals, colors, triangles, builder.image);
   }

   public static class_1011 decode(byte[] data) throws IOException {
      return decode(data, 2048);
   }

   public static class_1011 decode(byte[] data, int maxSize) throws IOException {
      ByteBuffer input = MemoryUtil.memAlloc(data.length);

      class_1011 var38;
      try {
         MemoryStack stack = MemoryStack.stackPush();

         try {
            input.put(data).flip();
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer c = stack.mallocInt(1);
            ByteBuffer pixels = STBImage.stbi_load_from_memory(input, w, h, c, 4);
            if (pixels == null) {
               throw new IOException("Could not read the texture: " + STBImage.stbi_failure_reason());
            }

            try {
               int width = w.get(0);
               int height = h.get(0);
               int step = 1;

               while (width / step > maxSize || height / step > maxSize) {
                  step *= 2;
               }

               int ow = Math.max(1, width / step);
               int oh = Math.max(1, height / step);
               class_1011 image = new class_1011(ow, oh, false);
               int area = step * step;

               for (int y = 0; y < oh; y++) {
                  for (int x = 0; x < ow; x++) {
                     int r = 0;
                     int g = 0;
                     int b = 0;
                     int a = 0;

                     for (int dy = 0; dy < step; dy++) {
                        for (int dx = 0; dx < step; dx++) {
                           int idx = ((y * step + dy) * width + x * step + dx) * 4;
                           r += pixels.get(idx) & 255;
                           g += pixels.get(idx + 1) & 255;
                           b += pixels.get(idx + 2) & 255;
                           a += pixels.get(idx + 3) & 255;
                        }
                     }

                     image.method_61941(x, y, a / area << 24 | r / area << 16 | g / area << 8 | b / area);
                  }
               }

               var38 = image;
            } finally {
               STBImage.stbi_image_free(pixels);
            }
         } catch (Throwable var36) {
            if (stack != null) {
               try {
                  stack.close();
               } catch (Throwable var34) {
                  var36.addSuppressed(var34);
               }
            }

            throw var36;
         }

         if (stack != null) {
            stack.close();
         }
      } finally {
         MemoryUtil.memFree(input);
      }

      return var38;
   }

   private static List<SkinLoader.Raw> obj(Path file) throws IOException {
      List<float[]> v = new ArrayList<>();
      List<float[]> vt = new ArrayList<>();
      List<float[]> vn = new ArrayList<>();
      Map<String, String[]> materials = new HashMap<>();
      Map<String, SkinLoader.Builder> builders = new LinkedHashMap<>();
      Map<String, class_1011> textures = new HashMap<>();
      String current = "";

      for (String line : Files.readAllLines(file)) {
         String trimmed = line.trim();
         if (!trimmed.isEmpty() && !trimmed.startsWith("#")) {
            String[] parts = trimmed.split("\\s+");
            String var12 = parts[0];
            switch (var12) {
               case "mtllib":
                  Path mtl = file.resolveSibling(trimmed.substring(7).trim());
                  if (Files.exists(mtl)) {
                     materials.putAll(mtl(mtl));
                  }
                  break;
               case "usemtl":
                  current = parts.length > 1 ? parts[1] : "";
                  break;
               case "v":
                  v.add(new float[]{Float.parseFloat(parts[1]), Float.parseFloat(parts[2]), Float.parseFloat(parts[3])});
                  break;
               case "vt":
                  vt.add(new float[]{Float.parseFloat(parts[1]), parts.length > 2 ? Float.parseFloat(parts[2]) : 0.0F});
                  break;
               case "vn":
                  vn.add(new float[]{Float.parseFloat(parts[1]), Float.parseFloat(parts[2]), Float.parseFloat(parts[3])});
                  break;
               case "f":
                  SkinLoader.Builder builder = builders.get(current);
                  if (builder == null) {
                     builder = new SkinLoader.Builder();
                     String[] material = materials.get(current);
                     if (material != null && material[1] != null) {
                        String texName = material[1];
                        if (!textures.containsKey(texName)) {
                           Path texFile = file.resolveSibling(texName);
                           textures.put(texName, Files.exists(texFile) ? decode(Files.readAllBytes(texFile)) : null);
                        }

                        builder.image = textures.get(texName);
                     }

                     builders.put(current, builder);
                  }

                  String[] material = materials.get(current);
                  int color = material != null && material[0] != null ? (int)Long.parseLong(material[0], 16) : -1;
                  int[] corner = new int[parts.length - 1];

                  for (int i = 1; i < parts.length; i++) {
                     String[] refs = parts[i].split("/");
                     int pi = index(refs[0], v.size());
                     float[] p = v.get(pi);
                     float u = 0.0F;
                     float w = 0.0F;
                     if (refs.length > 1 && !refs[1].isEmpty()) {
                        float[] t = vt.get(index(refs[1], vt.size()));
                        u = t[0];
                        w = 1.0F - t[1];
                     }

                     float nx = 0.0F / 0.0F;
                     float ny = 0.0F / 0.0F;
                     float nz = 0.0F / 0.0F;
                     if (refs.length > 2 && !refs[2].isEmpty()) {
                        float[] n = vn.get(index(refs[2], vn.size()));
                        nx = n[0];
                        ny = n[1];
                        nz = n[2];
                     }

                     corner[i - 1] = builder.vertices();
                     builder.vertex(p[0], p[1], p[2], u, w, nx, ny, nz, color);
                  }

                  for (int i = 1; i + 1 < corner.length; i++) {
                     builder.triangles.add(corner[0]);
                     builder.triangles.add(corner[i]);
                     builder.triangles.add(corner[i + 1]);
                  }
            }
         }
      }

      List<SkinLoader.Raw> result = new ArrayList<>();

      for (SkinLoader.Builder builder : builders.values()) {
         result.add(toRaw(builder));
      }

      return result;
   }

   private static int index(String ref, int size) {
      int i = Integer.parseInt(ref);
      return i < 0 ? size + i : i - 1;
   }

   private static Map<String, String[]> mtl(Path file) throws IOException {
      Map<String, String[]> result = new HashMap<>();
      String name = null;

      for (String line : Files.readAllLines(file)) {
         String trimmed = line.trim();
         String[] parts = trimmed.split("\\s+");
         if (parts.length != 0) {
            if (parts[0].equals("newmtl") && parts.length > 1) {
               name = parts[1];
               result.put(name, new String[2]);
            } else if (name != null && parts[0].equals("Kd") && parts.length > 3) {
               int r = clamp(Float.parseFloat(parts[1]));
               int g = clamp(Float.parseFloat(parts[2]));
               int b = clamp(Float.parseFloat(parts[3]));
               result.get(name)[0] = Integer.toHexString(0xFF000000 | r << 16 | g << 8 | b);
            } else if (name != null && parts[0].equals("map_Kd") && parts.length > 1) {
               result.get(name)[1] = parts[parts.length - 1];
            }
         }
      }

      return result;
   }

   private static List<SkinLoader.Raw> stl(byte[] bytes) throws IOException {
      SkinLoader.Builder builder = new SkinLoader.Builder();
      ByteBuffer buf = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
      if (bytes.length >= 84 && 84L + ((long)buf.getInt(80) & 4294967295L) * 50L == (long)bytes.length) {
         int count = buf.getInt(80);

         for (int t = 0; t < count; t++) {
            int base = 84 + t * 50 + 12;

            for (int k = 0; k < 3; k++) {
               builder.triangles.add(builder.vertices());
               builder.vertex(
                  buf.getFloat(base + k * 12),
                  buf.getFloat(base + k * 12 + 4),
                  buf.getFloat(base + k * 12 + 8),
                  0.0F,
                  0.0F,
                  0.0F / 0.0F,
                  0.0F / 0.0F,
                  0.0F / 0.0F,
                  -3617582
               );
            }
         }
      } else {
         for (String line : new String(bytes, StandardCharsets.US_ASCII).split("\\R")) {
            String trimmed = line.trim();
            if (trimmed.startsWith("vertex")) {
               String[] parts = trimmed.split("\\s+");
               if (parts.length >= 4) {
                  builder.triangles.add(builder.vertices());
                  builder.vertex(
                     Float.parseFloat(parts[1]),
                     Float.parseFloat(parts[2]),
                     Float.parseFloat(parts[3]),
                     0.0F,
                     0.0F,
                     0.0F / 0.0F,
                     0.0F / 0.0F,
                     0.0F / 0.0F,
                     -3617582
                  );
               }
            }
         }
      }

      if (builder.triangles.size() < 3) {
         throw new IOException("Empty model");
      } else {
         SkinLoader.Raw raw = toRaw(builder);
         SkinLoader.Raw zUp = rotateZUp(raw);
         return List.of(zUp);
      }
   }

   private static SkinLoader.Raw rotateZUp(SkinLoader.Raw raw) {
      float[] p = raw.positions;
      float[] out = new float[p.length];

      for (int i = 0; i < p.length; i += 3) {
         out[i] = p[i];
         out[i + 1] = p[i + 2];
         out[i + 2] = -p[i + 1];
      }

      return new SkinLoader.Raw(out, raw.uvs, null, raw.colors, raw.triangles, raw.image);
   }

   private static void smoothNormals(SkinLoader.Raw raw) {
      float[] p = raw.positions;
      float[] n = new float[p.length];
      int[] t = raw.triangles;

      for (int i = 0; i + 2 < t.length; i += 3) {
         int a = t[i] * 3;
         int b = t[i + 1] * 3;
         int c = t[i + 2] * 3;
         float ux = p[b] - p[a];
         float uy = p[b + 1] - p[a + 1];
         float uz = p[b + 2] - p[a + 2];
         float vx = p[c] - p[a];
         float vy = p[c + 1] - p[a + 1];
         float vz = p[c + 2] - p[a + 2];
         float nx = uy * vz - uz * vy;
         float ny = uz * vx - ux * vz;
         float nz = ux * vy - uy * vx;

         for (int k : new int[]{a, b, c}) {
            n[k] += nx;
            n[k + 1] = n[k + 1] + ny;
            n[k + 2] = n[k + 2] + nz;
         }
      }

      for (int i = 0; i < n.length; i += 3) {
         float len = (float)Math.sqrt((double)(n[i] * n[i] + n[i + 1] * n[i + 1] + n[i + 2] * n[i + 2]));
         if (len > 1.0E-12F) {
            n[i] /= len;
            n[i + 1] = n[i + 1] / len;
            n[i + 2] = n[i + 2] / len;
         } else {
            n[i + 1] = 1.0F;
         }
      }

      raw.normals = n;
   }

   private static List<SkinLoader.Raw> decimate(List<SkinLoader.Raw> parts, int budget) {
      int total = 0;
      float[] min = new float[]{3.4028235E38F, 3.4028235E38F, 3.4028235E38F};
      float[] max = new float[]{-3.4028235E38F, -3.4028235E38F, -3.4028235E38F};

      for (SkinLoader.Raw part : parts) {
         total += part.triangles.length / 3;

         for (int i = 0; i < part.positions.length; i++) {
            min[i % 3] = Math.min(min[i % 3], part.positions[i]);
            max[i % 3] = Math.max(max[i % 3], part.positions[i]);
         }
      }

      List<SkinLoader.Raw> current = parts;
      if (total <= budget) {
         return parts;
      } else {
         float extent = Math.max(max[0] - min[0], Math.max(max[1] - min[1], max[2] - min[2]));
         if (extent <= 0.0F) {
            return parts;
         } else {
            List<SkinLoader.Raw> best = parts;
            float cells = 220.0F;

            for (int attempt = 0; attempt < 14; attempt++) {
               float cell = extent / cells;
               List<SkinLoader.Raw> result = new ArrayList<>();
               int count = 0;

               for (SkinLoader.Raw part : current) {
                  SkinLoader.Raw reduced = cluster(part, cell, min, Math.max(12.0F, cells * 0.35F));
                  result.add(reduced);
                  count += reduced.triangles.length / 3;
               }

               best = result;
               if (count <= budget) {
                  break;
               }

               cells *= 0.78F;
            }

            return best;
         }
      }
   }

   private static SkinLoader.Raw cluster(SkinLoader.Raw part, float cell, float[] min, float uvGrid) {
      int vertices = part.colors.length;
      Map<SkinLoader.Key, Integer> clusters = new HashMap<>();
      int[] map = new int[vertices];
      List<double[]> sums = new ArrayList<>();

      for (int v = 0; v < vertices; v++) {
         long ix = (long)((part.positions[v * 3] - min[0]) / cell) & 2097151L;
         long iy = (long)((part.positions[v * 3 + 1] - min[1]) / cell) & 2097151L;
         long iz = (long)((part.positions[v * 3 + 2] - min[2]) / cell) & 2097151L;
         int uu = (int)Math.floor((double)(part.uvs[v * 2] * uvGrid)) & 65535;
         int vv = (int)Math.floor((double)(part.uvs[v * 2 + 1] * uvGrid)) & 65535;
         SkinLoader.Key key = new SkinLoader.Key(ix | iy << 21 | iz << 42, uu | vv << 16);
         Integer id = clusters.get(key);
         if (id == null) {
            id = sums.size();
            clusters.put(key, id);
            sums.add(new double[13]);
         }

         map[v] = id;
         double[] s = sums.get(id);
         s[0] += (double)part.positions[v * 3];
         s[1] += (double)part.positions[v * 3 + 1];
         s[2] += (double)part.positions[v * 3 + 2];
         s[3] += (double)part.uvs[v * 2];
         s[4] += (double)part.uvs[v * 2 + 1];
         s[5] += (double)part.normals[v * 3];
         s[6] += (double)part.normals[v * 3 + 1];
         s[7] += (double)part.normals[v * 3 + 2];
         int c = part.colors[v];
         s[8] += (double)(c >>> 24);
         s[9] += (double)(c >> 16 & 0xFF);
         s[10] += (double)(c >> 8 & 0xFF);
         s[11] += (double)(c & 0xFF);
         s[12]++;
      }

      int n = sums.size();
      float[] positions = new float[n * 3];
      float[] uvs = new float[n * 2];
      float[] normals = new float[n * 3];
      int[] colors = new int[n];

      for (int i = 0; i < n; i++) {
         double[] s = sums.get(i);
         double k = s[12];
         positions[i * 3] = (float)(s[0] / k);
         positions[i * 3 + 1] = (float)(s[1] / k);
         positions[i * 3 + 2] = (float)(s[2] / k);
         uvs[i * 2] = (float)(s[3] / k);
         uvs[i * 2 + 1] = (float)(s[4] / k);
         double nx = s[5];
         double ny = s[6];
         double nz = s[7];
         double len = Math.sqrt(nx * nx + ny * ny + nz * nz);
         if (len < 1.0E-9) {
            len = 1.0;
            ny = 1.0;
         }

         normals[i * 3] = (float)(nx / len);
         normals[i * 3 + 1] = (float)(ny / len);
         normals[i * 3 + 2] = (float)(nz / len);
         colors[i] = (int)Math.round(s[8] / k) << 24 | (int)Math.round(s[9] / k) << 16 | (int)Math.round(s[10] / k) << 8 | (int)Math.round(s[11] / k);
      }

      List<Integer> triangles = new ArrayList<>();
      Set<Long> seen = new HashSet<>();

      for (int i = 0; i + 2 < part.triangles.length; i += 3) {
         int a = map[part.triangles[i]];
         int b = map[part.triangles[i + 1]];
         int c = map[part.triangles[i + 2]];
         if (a != b && b != c && a != c) {
            int lo = Math.min(a, Math.min(b, c));
            int hi = Math.max(a, Math.max(b, c));
            int mid = a + b + c - lo - hi;
            long id = (long)lo * 2654435761L ^ (long)mid << 21 ^ (long)hi << 42 ^ (long)(a == lo ? (b == mid ? 0 : 1) : 2);
            if (seen.add(id)) {
               triangles.add(a);
               triangles.add(b);
               triangles.add(c);
            }
         }
      }

      return new SkinLoader.Raw(positions, uvs, normals, colors, triangles.stream().mapToInt(Integer::intValue).toArray(), part.image);
   }

   private static void normalize(List<SkinLoader.Raw> parts) {
      float[] min = new float[]{3.4028235E38F, 3.4028235E38F, 3.4028235E38F};
      float[] max = new float[]{-3.4028235E38F, -3.4028235E38F, -3.4028235E38F};

      for (SkinLoader.Raw part : parts) {
         for (int i = 0; i < part.positions.length; i++) {
            min[i % 3] = Math.min(min[i % 3], part.positions[i]);
            max[i % 3] = Math.max(max[i % 3], part.positions[i]);
         }
      }

      float height = max[1] - min[1];
      if (height <= 1.0E-6F) {
         height = Math.max(1.0E-6F, Math.max(max[0] - min[0], max[2] - min[2]));
      }

      float cx = (min[0] + max[0]) / 2.0F;
      float cz = (min[2] + max[2]) / 2.0F;

      for (SkinLoader.Raw part : parts) {
         float[] p = part.positions;

         for (int i = 0; i < p.length; i += 3) {
            p[i] = (p[i] - cx) / height;
            p[i + 1] = (p[i + 1] - min[1]) / height;
            p[i + 2] = (p[i + 2] - cz) / height;
         }
      }
   }

   private static final class Builder {
      final List<Float> positions = new ArrayList<>();
      final List<Float> uvs = new ArrayList<>();
      final List<Float> normals = new ArrayList<>();
      final List<Integer> colors = new ArrayList<>();
      final List<Integer> triangles = new ArrayList<>();
      class_1011 image;

      int vertices() {
         return this.colors.size();
      }

      void vertex(float x, float y, float z, float u, float v, float nx, float ny, float nz, int color) {
         this.positions.add(x);
         this.positions.add(y);
         this.positions.add(z);
         this.uvs.add(u);
         this.uvs.add(v);
         this.normals.add(nx);
         this.normals.add(ny);
         this.normals.add(nz);
         this.colors.add(color);
      }
   }

   private static record Key(long position, int uv) {
   }

   private static final class Raw {
      final float[] positions;
      final float[] uvs;
      float[] normals;
      final int[] colors;
      final int[] triangles;
      final class_1011 image;

      Raw(float[] positions, float[] uvs, float[] normals, int[] colors, int[] triangles, class_1011 image) {
         this.positions = positions;
         this.uvs = uvs;
         this.normals = normals;
         this.colors = colors;
         this.triangles = triangles;
         this.image = image;
      }
   }
}
