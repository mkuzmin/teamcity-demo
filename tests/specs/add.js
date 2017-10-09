var assert = require('assert');

describe('Checklist', function() {
    it('should submit login form', function () {
        browser.url('/');
        $('form').waitForExist(5000);

        var count = browser.elements('span.description').value.length;
        var value = require("uuid").v4();
        browser.setValue('#newItem', value);
        browser.submitForm('#checklist');

        browser.waitUntil(function () {
            var l = browser.elements('span.description').value.length;
            return l == count + 1;
        }, 5000);

        var actualValue = browser.elementIdText(browser.elements('span.description').value[count].ELEMENT).value;
        assert.equal(actualValue, value)
    });
});
