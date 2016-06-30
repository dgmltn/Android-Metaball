Android-Metaball
================

[ ![Download](https://api.bintray.com/packages/dgmltn/maven/android-metaball/images/download.svg) ](https://bintray.com/dgmltn/maven/android-metaball/_latestVersion)

![screenshot](https://raw.github.com/dgmltn/Android-Metaball/master/art/screenshot.png)

## Summary

A metaball "current page" indicator. AKA ViewPagerIndicator. These metaballs act like water droplets with surface tension. Physics FTW!

This library was heavily inspired by [previous work](https://raw.githubusercontent.com/dodola/MetaballLoading/master/app/src/main/java/com/dodola/animview/MetaballView.java). Open Source FTW!

## Usage

```xml
  <!-- As a standalone view with custom options -->
  <com.dgmltn.metaball.MetaballView
      android:layout_width="wrap_content"
      android:layout_height="wrap_content"
      android:layout_gravity="center"
      android:layout_marginBottom="12dp"
      android:layout_marginTop="24dp"
      android:background="#20000000"
      app:mv_dotCount="5"
      app:mv_dotRadius="8dp"
      app:mv_selectedColor="#336699"
      app:mv_unselectedColor="#20336699" />

  <!-- As a ViewPager.DecorView -->
  <android.support.v4.view.ViewPager
      android:id="@+id/viewpager"
      android:layout_width="match_parent"
      android:layout_height="64dp"
      android:background="@android:color/white">

      <com.dgmltn.metaball.ViewPagerMetaballView
          android:id="@+id/viewpager_dots"
          android:layout_width="match_parent"
          android:layout_height="match_parent"
          android:layout_gravity="bottom|center_horizontal" />

  </android.support.v4.view.ViewPager>
```

## Obtaining

Include in your android project from jcenter, using Gradle:
```groovy
compile 'com.dgmltn:android-metaball:1.1.0'
```

## License

    Copyright 2015-2016 Doug Melton

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
