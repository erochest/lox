use std::borrow::{BorrowMut, Borrow};
use std::cell::{Cell, RefCell};
use std::rc::Rc;
use std::fmt::Debug;

type NodePtr<T> = Option<Box<Node<T>>>;

pub struct LinkedList<T> {
    head: Link<T>,
    length: usize,
}

#[derive(Debug, PartialEq)]
enum Link<T> {
    Empty,
    More(Box<Node<T>>),
}

#[derive(Debug, PartialEq)]
struct Node<T> {
    value: T,
    next: Link<T>,
}

impl<T> LinkedList<T>
where T: Debug + PartialEq {
    pub fn new() -> Self {
        Self {
            head: Link::Empty,
            length: 0,
        }
    }

    pub fn push(&mut self, value: T) {
        let new_node = Box::new(Node {
            value,
            next: Link::Empty,
        });

        match self.head.last_mut() {
            Some(node) => node.next = Link::More(new_node),
            None => self.head = Link::More(new_node),
        }

        self.length += 1;
    }

    fn get(&self, i: usize) -> Option<&T> {
        self.head.get(i)
    }
}

impl<T> Link<T> {
    fn get(&self, i: usize) -> Option<&T> where T: std::fmt::Debug {
        // println!("self: {:?} / i: {}", self, i);
        if let Link::More(ref node) = self {
            if i == 0 {
                Some(&node.value)
            } else {
                node.next.get(i - 1)
            }
        } else {
            None
        }
    }

    fn tail(&self) -> Option<&Link<T>> {
        if let Link::More(ref node) = self {
            Some(&node.next)
        } else {
            None
        }
    }

    fn last(&self) -> Option<&Node<T>> {
        if let Link::More(ref node) = self {
            node.last()
        } else {
            None
        }
    }

    fn last_mut(&mut self) -> Option<&mut Node<T>> {
        if let Link::More(ref mut node) = self {
            node.last_mut()
        } else {
            None
        }
    }
}

impl<T> Node<T> {
    fn last(&self) -> Option<&Node<T>> {
        if let Link::More(ref node) = self.next {
            node.last()
        } else {
            Some(self)
        }
    }

    fn last_mut(&mut self) -> Option<&mut Node<T>> {
        if let Link::More(ref mut node) = self.next {
            node.last_mut()
        } else {
            Some(self)
        }
    }
}

#[cfg(test)]
mod tests;