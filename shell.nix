{ pkgs ? import <nixpkgs> {config.allowUnfree = true;} }:
pkgs.mkShell {
  buildInputs = with pkgs; [ jdk21 kotlin gradle ];
}
