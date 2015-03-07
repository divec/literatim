# literatim

literatim is an Android predictive text keyboard powered by a bilingual
dictionary. See
http://troi.org/en/literatim.html

## Installing

You can install from Google Play:
https://play.google.com/store/apps/details?id=org.troi.literatim

Or build locally and install (see below)

## Developing

First install Android SDK 7, then:

```shell
$ android update project -p . # generate local.properties & build.properties
$ ant debug
$ ant debug install
$ ant release
$ ant release install
```
