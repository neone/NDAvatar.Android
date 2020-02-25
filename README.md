![BannerImage](./Images/NDAvatar.AndroidGitHubBanner.png)

## Description

The NDAvatar framework provides a unified way of setting and displaying user avatars based on their name or profile picture. Customization options include adding a border, border width, and border color, as well as setting the frame's corner radius or creating a circular mask. You can see example usage of the framework in NDAvatarAndroidSample. The sample application does not use all functionality but hits some of the more important features for our team.

## Design Goal

This framework is simply about having a view you can feed a name and/or picture and it displays a rounded crop version with either the pic or user initials.

## Screenshots/Demo

![Demo](./demoImages/ndavatarDemo.gif)

## Requirements for Use
- Android: minSdkVersion 26
- Android: targetSdkVersion 29
- Kotlin: Compiled/Tested with 1.3+

## Features
- 100% Kotlin
- Rectangular or circular avatar view: Selectable with a boolean flag
- Avatar Framing:
  - Control border width
  - Control border fill color
  - Control background filler color (for use with alpha-masked image or avatar image)
- Imageless Avatar: A users name can be passed in and used to generate drawn-text initials for a nice avatar image
- Set Text Color for drawing initials
- Set Alpha mask for image/initial bitmap layer
- Thanks to Henning Dodenhof: https://github.com/hdodenhof/CircleImageView
  - `It uses a BitmapShader and does not:`
    - `create a copy of the original bitmap`
    - `use a clipPath (which is neither hardware accelerated nor anti-aliased)`
    - `use setXfermode to clip the bitmap (which means drawing twice to the canvas)`
- [ ] Adjust corner radius: Supported but needs some basic implementation to expose functionality.
- [ ] Use gradient for background: Supported but needs some basic implementation to expose functionality.
- [ ] Pick images from file picker: Currently the imageUri can be specified, but no user-facing picker is implemented
- [ ] Use camera to take an image to use for avatar: Not implemented yet.
- [ ] User-facing control of cropping: The current implementation will exclusively center-crop image passed in



## Attribution

- Henning Dodenhof https://github.com/hdodenhof/CircleImageView
- Amulyakhare https://github.com/amulyakhare/TextDrawable

## Integration
### MODULE BUILD.GRADLE
```
repositories {
    maven { url  "https://dl.bintray.com/neoneoperations/NDAvatarAndroid" }
}
dependencies {
    // LOCAL REFERENCE TO LIBRARY
    //implementation project(':ndavatar')

    // REMOTE REFERENCE TO PUBLISHED LIBRARY
    implementation(group: 'com.neone.android', name: 'ndavatar', version: '1.0.0', ext: 'aar', classifier: '')
}
```
