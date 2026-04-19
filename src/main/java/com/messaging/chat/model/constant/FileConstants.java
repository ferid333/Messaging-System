package com.messaging.chat.model.constant;


import java.util.Set;

public class FileConstants {

    private FileConstants() {
    }

    public static final Set<String> EXECUTABLE_CONTENT_TYPES = Set.of(
            "application/java-archive",
            "application/octet-stream",
            "application/vnd.android.package-archive",
            "application/vnd.microsoft.portable-executable",
            "application/x-bat",
            "application/x-csh",
            "application/x-dosexec",
            "application/x-executable",
            "application/x-ms-installer",
            "application/x-msdos-program",
            "application/x-msdownload",
            "application/x-sh",
            "application/x-shellscript",
            "application/x-shockwave-flash",
            "application/x-windows-executable",
            "text/javascript"
    );

    public static final Set<String> EXECUTABLE_FILE_EXTENSIONS = Set.of(
            "apk",
            "appimage",
            "bat",
            "bin",
            "cmd",
            "com",
            "csh",
            "dll",
            "dmg",
            "exe",
            "jar",
            "js",
            "msi",
            "ps1",
            "run",
            "scr",
            "sh",
            "vb",
            "vbs",
            "wsf"
    );
}
