[ ![Download](https://api.bintray.com/packages/ebolo/ebolo-oss/krichtexteditor/images/download.svg)](https://bintray.com/ebolo/ebolo-oss/krichtexteditor/_latestVersion)
[![](https://jitpack.io/v/daothanhduy305/KRichEditor.svg)](https://jitpack.io/#daothanhduy305/KRichEditor)
[![Build Status](https://travis-ci.org/daothanhduy305/KRichEditor.svg?branch=master)](https://travis-ci.org/daothanhduy305/KRichEditor)

# KRichEditor

A rich text editor (based on [QuillJs](https://quilljs.com/) and [MRichTextEditor](https://github.com/Even201314/MRichEditor/)) ported to Kotlin

## Install

Add to root Gradle:

    allprojects {
      repositories {
          google()
          jcenter()
          ...
          maven { url 'http://dl.bintray.com/ebolo/ebolo-oss' }
          ...
      }
    }

Gradle:

    implementation 'com.ebolo:krichtexteditor:0.0.1'

## Features

- Bold
- Italic
- Underline
- Strike-through
- Heading 1
- Heading 2
- Heading 3
- Heading 4
- Heading 5
- Heading 6
- Paragraph
- Quote
- Ordered List
- Unordered List
- Code
- Horizontal Rule
- Link
- Image
- Justify Center
- Justify Full
- Justify Left
- Justify Right
- Subscript
- Superscript
- Font Name
- Font Size
- Indent
- Outdent
- Undo
- Redo

## Screenshot

<img src="ss/1.jpg" width=300/><img src="ss/2.jpg" width=300/><img src="ss/3.jpg" width=300/><img src="ss/4.jpg" width=300/>

## Usage

Kotlin simple - simply initialize this from any Context:

    val editorFragment = kRichEditorFragment()

Kotlin advanced - you can customize it, too:

    val editorFragment = kRichEditorFragment {
        // Customization block
    }
        
## Customizations

- To change activated/deactivated state colors:

        formatButtonActivatedColor = R.color.customActivatedColor
        formatButtonDeactivatedColor = R.color.customDeactivatedColor
        
- ...more to come...

## License

```
The MIT License (MIT)

Copyright © 2017 daothanhduy305,

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
```
