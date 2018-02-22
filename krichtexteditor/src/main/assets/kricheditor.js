var quill;

var initEditor = function(){
    var placeholderText = KRichEditor.getPlaceHolder();
    var config = {
        theme: 'snow',
        modules: {
            toolbar: false
        },
        placeholder: placeholderText
    };
    quill = new Quill('#editor', config);
    quill.on('selection-change', function(range, oldRange, source) {
      if (range) {
        updateCurrentStyle();
      } else {
        console.log('Cursor not in the editor');
      }
    });
    var initContents = KRichEditor.getInitContents();
    console.log(initContents)
    if (initContents && initContents != "") {
        quill.setContents(JSON.parse(initContents))
    }
    quill.focus();
};

function focus() { quill.focus(); }
function undo() { quill.history.undo(); }
function redo() { quill.history.redo(); }
function disable() { quill.disable(); }
function enable() { quill.enable(); }

function bold(state) {
    format( function() {
        var isBold = quill.getFormat().bold;
        quill.format('bold', !isBold, 'api');
    }, state );
}

function italic(state) {
    format( function() {
        var isItalic = quill.getFormat().italic;
        quill.format('italic', !isItalic, 'api');
    }, state );
}

function underline(state) {
    format( function() {
        var isUnderline = quill.getFormat().underline;
        quill.format('underline', !isUnderline, 'api');
    }, state );
}

function strikethrough(state) {
    format( function() {
        var isStrike = quill.getFormat().strike;
        quill.format('strike', !isStrike, 'api');
    }, state );
}

function script(style, state) {
    format( function() {
        if (quill.getFormat().script === style) quill.format('script', '', 'api');
        else quill.format('script', style, 'api');
    }, state );
}

var fontName = function(fontName) {
    $('#summernote').summernote('fontName', fontName);
};

function fontSize(size) {
    format( function() { quill.format('size', size, 'api'); }, false );
}

function align(style, state) {
    format( function() { quill.format('align', style, 'api'); }, state );
}

function insertOrderedList() {
    if (quill.getFormat().list === 'ordered') quill.format('list', false, 'api');
    else quill.format('list', 'ordered', 'api');
}

function insertUnorderedList() {
    if (quill.getFormat().list === 'bullet') quill.format('list', false, 'api');
    else quill.format('list', 'bullet', 'api');
}

function insertCheckList() {
    if (quill.getFormat().list === 'unchecked') quill.format('list', false, 'api');
    else quill.format('list', 'unchecked', 'api');
}

function formatBlock(tagName) {
    if (tagName === 'blockquote') {
        var isQuoted = quill.getFormat().blockquote;
        quill.format('blockquote', !isQuoted, 'api');
    } else {
        var isCodeBlock = quill.getFormat()['code-block'];
        quill.format('code-block', !isCodeBlock, 'api');
    }
}

function header(level) {
    var headerLevel = quill.getFormat().header;
    if (headerLevel && headerLevel == level) quill.format('header', 0, 'api');
    else quill.format('header', level, 'api');
}

function codeView(state) {
    format( function() {
        var isCode = quill.getFormat().code;
        quill.format('code', !isCode, "api");
    }, state );
}

function background(newColor) { quill.format('background', newColor, 'api'); }
function color(newColor) { quill.format('color', newColor, 'api'); }
function indent() { quill.format('indent', '+1', 'api'); }
function outdent() { quill.format('indent', '-1', 'api'); }
function createLink(linkUrl) { quill.format('link', linkUrl, 'api'); }
function insertEmbed(index, type, value) { quill.insertEmbed(index, type, value, 'api'); }
function updateCurrentStyle() { KRichEditor.updateCurrentStyle(JSON.stringify(quill.getFormat())); }
function getStyle() { return quill.getFormat(); }
function getSelection() { return quill.getSelection(); }
function getHtml() { return quill.root.innerHTML; }
function getText() { return quill.getText(); }
function getContents() { return quill.getContents(); }
function setContents(data) { quill.setContents(data); }

function format(formatFunction, reFocus) {
    quill.disable();
    formatFunction();
    if (reFocus) {
        quill.enable();
        quill.focus();
    }
    updateCurrentStyle();
}