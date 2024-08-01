pub struct Scanner {
    input: String,
    start: usize,
    current: usize,
    line: usize,
}

impl Scanner {
    pub fn new(input: String) -> Self {
        Self {
            input,
            start: 0,
            current: 0,
            line: 1,
        }
    }

    pub fn with_line(input: String, line: usize) -> Self {
        Self {
            input,
            start: 0,
            current: 0,
            line,
        }
    }
}
