const HANDLERS = [
	function deleteNode(parentQuery, offset) {
		document.querySelector(parentQuery).childNodes[offset].remove();
	},
	function deleteElement(query) {
		document.querySelector(query).remove();
	},
	function insertComment(parentQuery, offset, value) {
		const parent = document.querySelector(parentQuery);
		parent.insertBefore(document.createComment(value), parent.childNodes[offset]);
	},
	function insertElement(parentQuery, offset, tagName, ... attributes) {
		const element = document.createElement(tagName);
		let index = 0;
		while (index < attributes.length) {
			const name = attributes[index ++];
			const value = attributes[index ++];
			element.setAttribute(name, value);
		}
		const parent = document.querySelector(parentQuery);
		parent.insertBefore(element, parent.childNodes[offset]);
	},
	function insertText(parentQuery, offset, value) {
		const parent = document.querySelector(parentQuery);
		parent.insertBefore(document.createTextNode(value), parent.childNodes[offset]);
	},
	function moveNode(oldParentQuery, oldOffset, newParentQuery, newOffset) {
		const target = document.querySelector(oldParentQuery).childNodes[oldOffset];
		const newParent = document.querySelector(newParentQuery);
		newParent.insertBefore(target, newParent.childNodes[newOffset]);
	},
	function moveElement(targetQuery, newParentQuery, newOffset) {
		const target = document.querySelector(targetQuery);
		const newParent = document.querySelector(newParentQuery);
		newParent.insertBefore(target, newParent.childNodes[newOffset]);
	},
	function removeAttribute(query, name) {
		document.querySelector(query).removeAttribute(name);
	},
	function setAttribute(query, name, value) {
		document.querySelector(query).setAttribute(name, value);
	},
	function setValue(parentQuery, offset, value) {
		document.querySelector(parentQuery).childNodes[offset].nodeValue = value;
	},
];

export function execute(events) {
	for (const [type, ... parameters] of events) {
		HANDLERS[type].apply(this, parameters);
	}
}