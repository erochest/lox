
use super::*;

use pretty_assertions::assert_eq;

#[test]
fn when_constructing_a_linked_list_then_the_list_is_empty() {
    let list = LinkedList::<i32>::new();
    assert_eq!(list.is_empty(), true);
}

#[ignore = "why not"]
#[test]
fn when_pushing_an_item_then_the_list_is_not_empty() {
}

#[ignore = "why not"]
#[test]
fn when_pushing_multiple_items_then_the_list_is_not_empty() {
}

#[ignore = "why not"]
#[test]
fn when_pushing_the_list_grows() {
}


#[ignore = "why not"]
#[test]
fn when_getting_an_item_then_the_item_is_returned() {
}

#[ignore = "why not"]
#[test]
fn when_find_returns_none_then_the_item_is_not_in_the_list() {
}

#[ignore = "why not"]
#[test]
fn when_find_returns_some_then_the_item_is_in_the_list() {
}

#[ignore = "why not"]
fn when_prepend_on_empty_list_then_the_list_is_not_empty() {
}

// TODO: prepend on not empty list
// TODO: insert after
// TODO: insert before
// TODO: delete
// TODO: pop
// TODO: pop-head