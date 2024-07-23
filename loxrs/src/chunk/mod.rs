use crate::value::ValueArray;

pub enum OpCode {
    OpConstant,
    OpReturn,
}

// create a struct to represent a chunk of bytecode
pub struct Chunk {
    // store the bytecode in a vector
    pub code: Vec<u8>,
    pub constants: ValueArray,
    pub lines: Vec<usize>,
}

/// Create a new chunk
impl Chunk {
    pub fn new() -> Chunk {
        Chunk {
            code: Vec::new(),
            constants: ValueArray::new(),
            lines: Vec::new(),
        }
    }

    /// Write a byte to the chunk
    pub fn write(&mut self, op_code: u8, line_no: usize) {
        self.code.push(op_code);
        self.lines.push(line_no);
    }

    pub fn add_constant(&mut self, value: f64) -> usize {
        self.constants.write(value);
        self.constants.values.len() - 1
    }

    /// Get the byte at the given index
    pub fn get(&self, index: usize) -> u8 {
        self.code[index]
    }
}

impl Default for Chunk {
    fn default() -> Self {
        Self::new()
    }
}
