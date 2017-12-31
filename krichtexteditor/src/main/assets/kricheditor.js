var quill;

var initEditor = function(){
    var initText = KRichEditor.getInitText();
    var config = {
        theme: 'snow',
        modules: {
            toolbar: false
        },
        placeholder: initText
    };
    quill = new Quill('#editor', config);
    quill.on('selection-change', function(range, oldRange, source) {
      if (range) {
        updateCurrentStyle();
      } else {
        console.log('Cursor not in the editor');
      }
    });
    quill.focus();
};

var focus = function(){
    quill.focus();
};

var undo = function() {
    quill.history.undo();
};

var redo = function() {
    quill.history.redo();
};

var disable = function() {
    quill.disable();
};

var enable = function() {
    quill.enable();
};

function bold() {
    format( function() {
        var isBold = quill.getFormat().bold;
        quill.format('bold', !isBold, "api");
    } );
};

function italic() {
    format( function() {
        var isItalic = quill.getFormat().italic;
        quill.format('italic', !isItalic, "api");
    } );
};

function underline() {
    format( function() {
        var isUnderline = quill.getFormat().underline;
        quill.format('underline', !isUnderline, "api");
    } );
};

function strikethrough() {
    format( function() {
        var isStrike = quill.getFormat().strike;
        quill.format('strike', !isStrike, "api");
    } );
};

function script(style) {
    format( function() {
        if (quill.getFormat().script === style) quill.format('script', '');
        else quill.format('script', style);
    } );
};

var background = function(color) {
    quill.format('background', color);
};

var color = function(color) {
    quill.format('color', color);
};

var fontName = function(fontName) {
    $('#summernote').summernote('fontName', fontName);
};

var fontSize = function(fontSize) {
    quill.format('size', fontSize);
    updateCurrentStyle();
};

var align = function(style) {
    quill.format('align', style);
    updateCurrentStyle();
};

var insertOrderedList = function() {
    if (quill.getFormat().list === 'ordered') quill.format('list', false);
    else quill.format('list', 'ordered');

};

var insertUnorderedList = function() {
    if (quill.getFormat().list === 'bullet') quill.format('list', false);
        else quill.format('list', 'bullet');
};

var indent = function() {
    quill.format('indent', '+1');
};

var outdent = function() {
    quill.format('indent', '-1');
};

var formatBlock = function(tagName) {
    if (tagName === 'blockquote') {
        var isQuoted = quill.getFormat().blockquote;
        quill.format('blockquote', !isQuoted);
    } else {
        var isCodeBlock = quill.getFormat()['code-block'];
        quill.format('code-block', !isCodeBlock);
    }
};

var header = function(level) {
    var headerLevel = quill.getFormat().header;
    if (headerLevel && headerLevel == level) quill.format('header', 0);
    else quill.format('header', level);
};

var insertImageUrl = function(imageUrl) {
    $('#summernote').summernote('insertImage', imageUrl, null);
};

var insertText = function(text) {
    $('#summernote').summernote('insertText', text);
};

var createLink = function(linkUrl) {
    quill.format('link', linkUrl)
};

function codeView() {
    format( function() {
        var isCode = quill.getFormat().code;
        quill.format('code', !isCode, "api");
    } );
};

var insertTable = function(dim){
    $('#summernote').summernote('insertTable', dim);
};

var updateCurrentStyle = function() {
    KRichEditor.updateCurrentStyle(JSON.stringify(quill.getFormat()));
};

var getSelection = function() {
    return quill.getSelection();
};

var getHtml = function() {
    return quill.root.innerHTML;
};

function format(formatFunction) {
    quill.enable(false);
    formatFunction();
    updateCurrentStyle();
    quill.enable(true);
    quill.focus();
};