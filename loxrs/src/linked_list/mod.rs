use std::{cell::RefCell, rc::Rc};


type Link<T> = Option<Rc<RefCell<Node<T>>>>;

struct Node<T> {
    item: T,
    next: Link<T>,
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
        }));

        match self.tail.take() {
            Some(old_tail) => {
                old_tail.borrow_mut().next = Some(new_node.clone());
                self.tail = Some(new_node);
            }
            None => {
                self.head = Some(new_node.clone());
                self.tail = Some(new_node);
            }
        }

        self.length += 1;
    }

    pub fn prepend(&mut self, item: T) {
        unimplemented!()
    }

    pub fn insert_after(&mut self, item: T) {
        unimplemented!()
    }

    pub fn insert_before(&mut self, item: T) {
        unimplemented!()
    }

    pub fn delete(&mut self, item: T) {
        unimplemented!()
    }

    pub fn pop(&mut self) -> Option<T> {
        unimplemented!()
    }

    pub fn pop_head(&mut self) -> Option<T> {
        unimplemented!()
    }

    pub fn find(&self, item: T) -> Option<T> {
        unimplemented!()
    }
}
#[cfg(test)]
mod tests;