# NDAvatarAndroid

## DESCRIPTION

The NDAvatar framework provides a unified way of setting and displaying user avatars based on their name or profile picture. Customization options include adding a border, border width, and border color, as well as setting the frame's corner radius or creating a circular mask. You can see example usage of the framework in NDAvatarAppAndroid.

## DESIGN GOAL

This framework is simply about having a view you can feed a name and/or picture and it displays a rounded crop version with either the pic or user initials.

## FEATURES

- No image => Render: {first initial}{last initial}
- Image => Crop to a circular image
- Framing:
  - Adjust border width
  - Add/remove border
  - Adust corner radius
