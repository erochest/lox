
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
    assert_eq!(list.get(0), Some(&1));
}

#[test]
fn when_pushing_multiple_items_then_the_list_is_not_empty() {
    let mut list: LinkedList<usize> = LinkedList::new();
    list.push(1);
    list.push(2);
    assert_eq!(list.length, 2);
    assert_eq!(list.get(0), Some(&1));
    assert_eq!(list.get(1), Some(&2));
}

#[test]
fn when_pushing_the_list_grows() {
    let mut list: LinkedList<usize> = LinkedList::new();
    list.push(1);
    list.push(2);
    assert_eq!(list.length, 2);
    list.push(3);
    assert_eq!(list.length, 3);
    assert_eq!(list.get(0), Some(&1));
    assert_eq!(list.get(1), Some(&2));
    assert_eq!(list.get(2), Some(&3));
}


#[test]
fn when_getting_an_item_then_the_item_is_returned() {
    let mut list: LinkedList<usize> = LinkedList::new();
    list.push(1);
    list.push(2);
    assert_eq!(list.get(0), Some(&1));
    assert_eq!(list.get(1), Some(&2));
}

#[test]
fn when_find_returns_none_then_the_item_is_not_in_the_list() {
    let mut list: LinkedList<usize> = LinkedList::new();
    list.push(1);
    list.push(2);
    assert_eq!(list.find(3), None);
}

// TODO: find
// TODO: prepend
// TODO: insert after
// TODO: insert before
// TODO: delete
// TODO: pop
// TODO: pop-head