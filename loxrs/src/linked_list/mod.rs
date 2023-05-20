use std::{cell::RefCell, rc::Rc};

type Link<T> = Option<Rc<RefCell<Node<T>>>>;

struct Node<T> {
    item: T,
    next: Link<T>,
    prev: Link<T>,
}

pub struct LinkedList<T> {
    head: Link<T>,
    tail: Link<T>,
    length: usize,
}

impl<T> LinkedList<T> {
    pub fn new() -> Self {
        LinkedList {
            head: None,
            tail: None,
            length: 0,
        }
    }

    pub fn is_empty(&self) -> bool {
        self.length == 0
    }

    pub fn push(&mut self, item: T) {
        let new_node = Rc::new(RefCell::new(Node {
            item,
            next: None,
            prev: None,
        }));

        match self.head.take() {
            Some(node) => {
                new_node.borrow_mut().next = Some(node.clone());
                node.borrow_mut().prev = Some(new_node.clone());
                self.head = Some(new_node);
            },
            None => {
                self.head = Some(new_node.clone());
                self.tail = Some(new_node);
            }
        }

        self.length += 1;
    }

    pub fn pop(&mut self) -> Option<T> {
        self.head.take().map(|node| {
            self.head = node.borrow_mut().next.take();
            if let Some(ref next) = self.head {
                next.borrow_mut().prev.take();
            } else {
                self.tail.take();
            }
            self.length -= 1;
            Rc::try_unwrap(node).ok().unwrap().into_inner().item
        })
    }

    pub fn append(&mut self, item: T) {
        let new_node = Rc::new(RefCell::new(Node {
            item,
            next: None,
            prev: None,
        }));

        match self.tail.take() {
            Some(node) => {
                new_node.borrow_mut().prev = Some(node.clone());
                node.borrow_mut().next = Some(new_node.clone());
                self.tail = Some(new_node);
            },
            None => {
                self.head = Some(new_node.clone());
                self.tail = Some(new_node);
            }
        }

        self.length += 1;
    }
}
#[cfg(test)]
mod tests;
