$ErrorActionPreference = 'Stop'
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$dir = $env:VIOLET_MEDIA_DIR
$parent = [int]$env:VIOLET_PARENT
$onlySpotify = $env:VIOLET_ONLY_SPOTIFY -eq '1'

Add-Type -TypeDefinition @'
using System;
using System.Runtime.InteropServices;

[ComImport, Guid("BCDE0395-E52F-467C-8E3D-C4579291692E")]
class YepDeviceEnumerator {}

[Guid("A95664D2-9614-4F35-A746-DE8DB63617E6"), InterfaceType(ComInterfaceType.InterfaceIsIUnknown)]
interface IYepDeviceEnumerator {
    int NotImpl1();
    [PreserveSig] int GetDefaultAudioEndpoint(int dataFlow, int role, out IYepDevice device);
}

[Guid("D666063F-1587-4E43-81F1-B948E807363F"), InterfaceType(ComInterfaceType.InterfaceIsIUnknown)]
interface IYepDevice {
    [PreserveSig] int Activate(ref Guid iid, int context, IntPtr parameters, [MarshalAs(UnmanagedType.IUnknown)] out object result);
}

[Guid("77AA99A0-1BD6-484F-8BC7-2C654C9A9B6F"), InterfaceType(ComInterfaceType.InterfaceIsIUnknown)]
interface IYepSessionManager {
    int NotImpl1();
    int NotImpl2();
    [PreserveSig] int GetSessionEnumerator(out IYepSessionEnumerator sessions);
}

[Guid("E2F5BB11-0570-40CA-ACDD-3AA01277DEE8"), InterfaceType(ComInterfaceType.InterfaceIsIUnknown)]
interface IYepSessionEnumerator {
    [PreserveSig] int GetCount(out int count);
    [PreserveSig] int GetSession(int index, out IYepSessionControl session);
}

[Guid("bfb7ff88-7239-4fc9-8fa2-07c950be9c6d"), InterfaceType(ComInterfaceType.InterfaceIsIUnknown)]
interface IYepSessionControl {
    int NotImpl0(); int NotImpl1(); int NotImpl2(); int NotImpl3(); int NotImpl4();
    int NotImpl5(); int NotImpl6(); int NotImpl7(); int NotImpl8();
    [PreserveSig] int GetSessionIdentifier([MarshalAs(UnmanagedType.LPWStr)] out string id);
    [PreserveSig] int GetSessionInstanceIdentifier([MarshalAs(UnmanagedType.LPWStr)] out string id);
    [PreserveSig] int GetProcessId(out uint pid);
}

[Guid("87CE5498-68D6-44E5-9215-6DA47EF883D8"), InterfaceType(ComInterfaceType.InterfaceIsIUnknown)]
interface IYepSimpleVolume {
    [PreserveSig] int SetMasterVolume(float level, ref Guid context);
    [PreserveSig] int GetMasterVolume(out float level);
}

public static class YepWindow {
    [DllImport("user32.dll")]
    static extern IntPtr SendMessage(IntPtr hWnd, int msg, IntPtr wParam, IntPtr lParam);

    public static void Command(IntPtr hwnd, int command) {
        SendMessage(hwnd, 0x0319, hwnd, new IntPtr(command << 16));
    }
}

public static class YepAudio {
    static System.Collections.Generic.List<IYepSimpleVolume> Find(string name) {
        var found = new System.Collections.Generic.List<IYepSimpleVolume>();
        try {
            var enumerator = (IYepDeviceEnumerator)(new YepDeviceEnumerator());
            IYepDevice device;
            if (enumerator.GetDefaultAudioEndpoint(0, 1, out device) != 0) return found;
            Guid iid = typeof(IYepSessionManager).GUID;
            object result;
            if (device.Activate(ref iid, 23, IntPtr.Zero, out result) != 0) return found;
            IYepSessionEnumerator sessions;
            ((IYepSessionManager)result).GetSessionEnumerator(out sessions);
            int count;
            sessions.GetCount(out count);
            for (int i = 0; i < count; i++) {
                IYepSessionControl control;
                sessions.GetSession(i, out control);
                uint pid;
                control.GetProcessId(out pid);
                try {
                    var process = System.Diagnostics.Process.GetProcessById((int)pid);
                    if (process.ProcessName.Equals(name, StringComparison.OrdinalIgnoreCase)) found.Add((IYepSimpleVolume)control);
                } catch {}
            }
        } catch {}
        return found;
    }

    public static float Get(string name) {
        foreach (var volume in Find(name)) {
            float level;
            if (volume.GetMasterVolume(out level) == 0) return level;
        }
        return -1f;
    }

    public static void Set(string name, float level) {
        Guid context = Guid.Empty;
        foreach (var volume in Find(name)) volume.SetMasterVolume(Math.Max(0f, Math.Min(1f, level)), ref context);
    }
}
'@

Add-Type -AssemblyName System.Runtime.WindowsRuntime
$asTask = [System.WindowsRuntimeSystemExtensions].GetMethods() | Where-Object { $_.Name -eq 'AsTask' -and $_.GetParameters().Count -eq 1 -and $_.GetParameters()[0].ParameterType.Name -eq 'IAsyncOperation`1' } | Select-Object -First 1
function Await($operation, [Type]$type) {
    $task = $asTask.MakeGenericMethod($type).Invoke($null, @($operation))
    if (-not $task.Wait(4000)) { throw 'Media request timed out' }
    return $task.Result
}
function Get-SpotifyWindow {
    foreach ($candidate in [System.Diagnostics.Process]::GetProcessesByName('Spotify')) {
        if ($candidate.MainWindowHandle -ne [IntPtr]::Zero -and $candidate.MainWindowTitle -ne '') { return $candidate }
    }
    return $null
}
$null = [Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager, Windows.Media.Control, ContentType = WindowsRuntime]
$null = [Windows.Storage.Streams.IRandomAccessStreamWithContentType, Windows.Storage.Streams, ContentType = WindowsRuntime]
$null = [Windows.Storage.Streams.IInputStream, Windows.Storage.Streams, ContentType = WindowsRuntime]
$asStream = [System.IO.WindowsRuntimeStreamExtensions].GetMethod('AsStreamForRead', [Type[]]@([Windows.Storage.Streams.IInputStream]))
$manager = $null
function Connect {
    try {
        $script:manager = Await ([Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager]::RequestAsync()) ([Windows.Media.Control.GlobalSystemMediaTransportControlsSessionManager])
    } catch {
        $script:manager = $null
    }
}
Connect

function Write-State($data) {
    [Console]::Out.WriteLine(($data | ConvertTo-Json -Compress))
    [Console]::Out.Flush()
}

$lastKey = ''
$cover = ''
$counter = 0
$failures = 0
$windowTitle = ''
$windowArtist = ''
while ($true) {
    try {
        $null = [System.Diagnostics.Process]::GetProcessById($parent)
    } catch {
        exit
    }
    try {
        if ($manager -eq $null) { Connect }
        $session = $null
        if ($manager -ne $null) {
            foreach ($candidate in $manager.GetSessions()) {
                if ($candidate.SourceAppUserModelId -like '*Spotify*') { $session = $candidate; break }
            }
            if ($session -eq $null -and -not $onlySpotify) { $session = $manager.GetCurrentSession() }
        }
        $window = $null
        if ($session -eq $null) { $window = Get-SpotifyWindow }
        $app = ''
        $processName = 'Spotify'
        if ($session -ne $null) {
            $app = [string]$session.SourceAppUserModelId
            if ($app -notlike '*Spotify*') { $processName = (($app -split '!')[0] -replace '\.exe$', '') }
        }

        foreach ($file in (Get-ChildItem -LiteralPath $dir -Filter 'cmd_*.txt' | Sort-Object Name)) {
            $command = (Get-Content -LiteralPath $file.FullName -Raw).Trim()
            Remove-Item -LiteralPath $file.FullName -Force
            $parts = $command -split ' '
            switch ($parts[0]) {
                'next' {
                    if ($session -ne $null) { $null = Await ($session.TrySkipNextAsync()) ([bool]) }
                    elseif ($window -ne $null) { [YepWindow]::Command($window.MainWindowHandle, 11) }
                }
                'previous' {
                    if ($session -ne $null) { $null = Await ($session.TrySkipPreviousAsync()) ([bool]) }
                    elseif ($window -ne $null) { [YepWindow]::Command($window.MainWindowHandle, 12) }
                }
                'toggle' {
                    if ($session -ne $null) { $null = Await ($session.TryTogglePlayPauseAsync()) ([bool]) }
                    elseif ($window -ne $null) { [YepWindow]::Command($window.MainWindowHandle, 14) }
                }
                'seek' { if ($session -ne $null) { $null = Await ($session.TryChangePlaybackPositionAsync([long]([double]::Parse($parts[1], [Globalization.CultureInfo]::InvariantCulture) * 10000000))) ([bool]) } }
                'volume' { [YepAudio]::Set($processName, [float]::Parse($parts[1], [Globalization.CultureInfo]::InvariantCulture)) }
            }
        }

        if ($session -ne $null) {
            $properties = Await ($session.TryGetMediaPropertiesAsync()) ([Windows.Media.Control.GlobalSystemMediaTransportControlsSessionMediaProperties])
            $timeline = $session.GetTimelineProperties()
            $info = $session.GetPlaybackInfo()
            $key = "$($properties.Title)|$($properties.Artist)|$($properties.AlbumTitle)"
            if ($key -ne $lastKey) {
                $lastKey = $key
                if ($cover -ne '' -and (Test-Path -LiteralPath $cover)) { Remove-Item -LiteralPath $cover -Force }
                $cover = ''
                if ($properties.Thumbnail -ne $null) {
                    try {
                        $stream = Await ($properties.Thumbnail.OpenReadAsync()) ([Windows.Storage.Streams.IRandomAccessStreamWithContentType])
                        $reader = $asStream.Invoke($null, @($stream))
                        $counter++
                        $path = Join-Path $dir ('cover_' + $counter + '.img')
                        $part = $path + '.part'
                        $output = [System.IO.File]::Create($part)
                        $reader.CopyTo($output)
                        $output.Close()
                        $reader.Close()
                        Move-Item -LiteralPath $part -Destination $path -Force
                        $cover = $path
                    } catch {}
                }
            }
            Write-State @{
                active = $true
                title = [string]$properties.Title
                artist = [string]$properties.Artist
                album = [string]$properties.AlbumTitle
                playing = ($info.PlaybackStatus.ToString() -eq 'Playing')
                position = $timeline.Position.TotalSeconds
                duration = ($timeline.EndTime - $timeline.StartTime).TotalSeconds
                updated = $timeline.LastUpdatedTime.ToUnixTimeMilliseconds()
                cover = $cover
                app = $app
                volume = [YepAudio]::Get($processName)
            }
        } elseif ($window -ne $null) {
            $name = [string]$window.MainWindowTitle
            $playing = $false
            if ($name -match '^(.+?) - (.+)$' -and $name -notmatch '^Spotify( Free| Premium)?$') {
                $windowArtist = $Matches[1]
                $windowTitle = $Matches[2]
                $playing = $true
            }
            if ($windowTitle -eq '') {
                Write-State @{ active = $false }
            } else {
                Write-State @{
                    active = $true
                    title = $windowTitle
                    artist = $windowArtist
                    album = ''
                    playing = $playing
                    position = 0
                    duration = 0
                    updated = 0
                    cover = ''
                    app = 'Spotify'
                    volume = [YepAudio]::Get('Spotify')
                }
            }
        } else {
            Write-State @{ active = $false }
        }
        $failures = 0
    } catch {
        $failures++
        if ($failures -ge 8) {
            $failures = 0
            $manager = $null
        }
        Write-State @{ error = [string]$_.Exception.Message }
    }
    Start-Sleep -Milliseconds 250
}
