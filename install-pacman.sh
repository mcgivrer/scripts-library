#!/bin/bash

cd /tmp
git clone -n --depth=1 --filter=blob:none https://github.com/git-for-windows/git-sdk-64.git
cd git-sdk-64
git sparse-checkout set --no-cone etc/pacman.d etc/pacman.conf usr/bin/pacman.exe var/lib/pacman
git checkout
cp -rf ./etc ./usr ./var /
pacman --noconfirm -Syy pacman 2>/dev/null
cd ..
rm -rf git-sdk-64

