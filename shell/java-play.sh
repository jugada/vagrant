#!/usr/bin/env bash
# This bootstraps Play Framework on CentOS 6.x

set -e

REPO_URL="http://downloads.typesafe.com/play/2.1.1/play-2.1.1.zip"

if [ "$EUID" -ne "0" ]; then
  echo "This script must be run as root." >&2
  exit 1
fi

# Install java play framework
echo "Installing Java"
yum -y install java
yum -y install unzip

if [ ! -d "/etc/play" ]; then
  mkdir /etc/play
fi

if [ ! -f play-2.1.1.zip ]; then
  echo "Downloading Play Framework..."
  wget -q ${REPO_URL}
fi

if [ ! -d /etc/play/play-2.1.1 ]; then
  echo "unziping play framework"
  unzip -o play-2.1.1.zip -d /etc/play
fi

echo "export the Play Framework to path"
echo 'export PATH=$PATH:/etc/play/play-2.1.1' >> $HOME/.bash_profile
. $HOME/.bash_profile
echo $PATH
echo "Play Framework installed!"
