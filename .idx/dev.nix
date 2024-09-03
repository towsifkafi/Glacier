{ pkgs, ... }: {

  # Which nixpkgs channel to use.
  channel = "stable-23.11"; # or "unstable"

  # Use https://search.nixos.org/packages to find packages
  packages = [
    pkgs.nodejs_18
    pkgs.zulu17
    pkgs.maven

    pkgs.starship
  ];

  # Search for the extensions you want on https://open-vsx.org/ and use "publisher.id"
  idx.extensions = [
    "angular.ng-template"
    "vscjava.vscode-java-dependency"
    "vscjava.vscode-java-pack"
    "redhat.java"
    "vscjava.vscode-maven"
  ];

  # Sets environment variables in the workspace
  env = {  };

}