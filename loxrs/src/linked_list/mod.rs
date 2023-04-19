use std::borrow::{BorrowMut, Borrow};
use std::cell::{Cell, RefCell};
use std::rc::Rc;

type NodePtr<T> = Option<Box<Node<T>>>;

pub struct LinkedList<T> {
    head: Link<T>,
    length: usize,
}

#[derive(Debug)]
enum Link<T> {
    Empty,
    More(Box<Node<T>>),
}

#[derive(Debug)]
struct Node<T> {
    value: T,
    next: Link<T>,
}

impl<T> LinkedList<T> {
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

        match self.head {
            Link::Empty => self.head = Link::More(new_node),
            Link::More(ref mut node) => {
                node.next = Link::More(new_node);
            }
        }

        self.length += 1;
    }

       fn get(&self, i: usize) -> Option<&T> where T: std::fmt::Debug  {
        self.head.get(i)
    }
}

impl<T> Link<T> {
    fn get(&self, i: usize) -> Option<&T> where T: std::fmt::Debug {
        println!("self: {:?} / i: {}", self, i);
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
}

#[cfg(test)]
mod tests;