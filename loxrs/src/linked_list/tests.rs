
use super::*;

use pretty_assertions::assert_eq;

// I want to write a series of tests to implement a linked list.
// I want to start with a test for the constructor.

#[test]
fn when_constructing_a_linked_list_then_the_list_is_empty() {
    let list: LinkedList<usize> = LinkedList::new();
    assert_eq!(list.length, 0);
}

#[test]
fn when_pushing_an_item_then_the_list_is_not_empty() {
    let mut list: LinkedList<usize> = LinkedList::new();
    list.push(1);
    assert_eq!(list.length, 1);
}