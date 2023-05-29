
pub struct List {
    head: Link,
}

impl List {
    pub fn new() -> Self {
        List { head: Link::Empty }
    }
}

enum Link {
    Empty,
    More(i32, Box<Node>),
}

struct Node {
    item: i32,
    next: List,
}

#[cfg(tests)]
mod tests;