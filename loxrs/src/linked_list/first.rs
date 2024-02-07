use std::mem;

pub struct List {
    head: Link,
}

impl List {
    pub fn new() -> Self {
        List { head: Link::Empty }
    }

    pub fn push(&mut self, elem: i32) {
        let new_node = Box::new(Node {
            item: elem,
            next: mem::replace(&mut self.head, Link::Empty),
        });
        self.head = Link::More(new_node);
    }

    pub fn pop(&mut self) -> Option<i32> {
        match self.head {
            Link::Empty => todo!(),
            Link::More(ref mut node) => {
                let node = mem::replace(node, Link::Empty);
                Some(node.item)
            }
        }
    }
}

enum Link {
    Empty,
    More(Box<Node>),
}

struct Node {
    item: i32,
    next: Link,
}

#[cfg(tests)]
mod tests;