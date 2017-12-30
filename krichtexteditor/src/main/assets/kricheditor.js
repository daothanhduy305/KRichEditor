var quill;

var initEditor = function(){
    var initText = KRichEditor.getInitText();
    var config = {
        "theme": "snow",
        "modules": {
            "toolbar": false
        },
        "placeholder": initText,
        "strict": false
    };
    quill = new Quill('#editor', config);
    quill.on('editor-change', function() {
        updateCurrentStyle();
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

var bold = function() {
    var isBold = quill.getFormat().bold;
    quill.format('bold', !isBold);
    updateCurrentStyle();
};

var italic = function() {
    var isItalic = quill.getFormat().italic;
    quill.format('italic', !isItalic);
    updateCurrentStyle();
};

var underline = function() {
    var isUnderline = quill.getFormat().underline;
    quill.format('underline', !isUnderline);
    updateCurrentStyle();
};

var strikethrough = function() {
    var isStrike = quill.getFormat().strike;
    quill.format('strike', !isStrike);
    updateCurrentStyle();
};

var script = function(style) {
    if (quill.getFormat().script === style) quill.format('script', '');
    else quill.format('script', style);
    updateCurrentStyle();
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
}

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

var lineHeight = function(lineHeight) {
    $('#summernote').summernote('lineHeight', lineHeight);
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

var unlink = function() {
    $('#summernote').summernote('unlink');
};

var insertText = function(text) {
    $('#summernote').summernote('editor.insertText', text);
};

var codeView = function(){
    if (quill.getFormat().code) quill.format('code', false);
    else quill.format('code', true);
    updateCurrentStyle();

};

var insertTable = function(dim){
    $('#summernote').summernote('insertTable', dim);
};

var insertHorizontalRule = function() {
    $('#summernote').summernote('insertHorizontalRule');
};

var pasteHTML = function(html){
    $('#summernote').summernote('code',html);
    keepLastIndex(document.getElementsByClassName('note-editable panel-body')[0]);
};

function keepLastIndex(obj) {
    var range = window.getSelection();
    range.selectAllChildren(obj);
    range.collapseToEnd();
};

var refreshHTML = function(){
    KRichEditor.returnHtml($('#summernote').summernote('code'));
};

var updateCurrentStyle = function() {
    KRichEditor.updateCurrentStyle(JSON.stringify(quill.getFormat()));
};

var getSelection = function() {
    return quill.getSelection();
}