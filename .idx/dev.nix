# Project IDX Environment Configuration
{ pkgs, ... }: {
  # Which nixpkgs channel to use.
  channel = "stable-23.11";

  # Use https://search.nixos.org/packages to find packages
  packages = [
    pkgs.jdk17
    pkgs.gradle_8  # Enforce Gradle 8 to avoid 9.0 incompatibilities
    pkgs.unzip
    pkgs.zip
  ];

  # Sets environment variables in the workspace
  env = {
    ANDROID_HOME = "/home/user/android-sdk";
    JAVA_HOME = "${pkgs.jdk17}";
  };

  idx = {
    # Search for the extensions you want on https://open-vsx.org/ and use "publisher.id"
    extensions = [
      "fwcd.kotlin"
      "vscjava.vscode-java-pack"
      "vscjava.vscode-gradle"
      "mathiasfrohlich.Kotlin"
    ];

    # Enable previews
    previews = {
      enable = true;
      previews = {
        # The Android emulator preview
        android = {
          command = ["./gradlew" "assembleDebug"];
          manager = "android";
        };
      };
    };

    # Workspace lifecycle hooks
    workspace = {
      # Runs when a workspace is first created
      onCreate = {
        # Example: install dependencies
        # install-deps = "./gradlew dependencies";
      };
      # Runs when the workspace is (re)started
      onStart = {
        # Example: start a build
        # build = "./gradlew assembleDebug";
      };
    };
  };
}
