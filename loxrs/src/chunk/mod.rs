use std::convert::TryFrom;

use crate::error::Error;
use crate::value::Value;

pub enum OpCode {
    OpConstant,
    OpAdd,
    OpSubtract,
    OpMultiply,
    OpDivide,
    OpNegate,
    OpReturn,
}

impl TryFrom<u8> for OpCode {
    type Error = Error;

    fn try_from(value: u8) -> Result<Self, Error> {
        match value {
            0 => Ok(OpCode::OpConstant),
            1 => Ok(OpCode::OpAdd),
            2 => Ok(OpCode::OpSubtract),
            3 => Ok(OpCode::OpMultiply),
            4 => Ok(OpCode::OpDivide),
            5 => Ok(OpCode::OpNegate),
            6 => Ok(OpCode::OpReturn),
            _ => Err(Error::InvalidOpCode(value)),
        }
    }
}

// create a struct to represent a chunk of bytecode
pub struct Chunk {
    // store the bytecode in a vector
    pub code: Vec<u8>,
    pub constants: Vec<Value>,
    pub lines: Vec<usize>,
}

/// Create a new chunk
impl Chunk {
    pub fn new() -> Chunk {
        Chunk {
            code: Vec::new(),
            constants: Vec::new(),
            lines: Vec::new(),
        }
    }

    /// Write a byte to the chunk
    pub fn write(&mut self, op_code: u8, line_no: usize) {
        self.code.push(op_code);
        self.lines.push(line_no);
    }

    pub fn add_constant(&mut self, value: f64) -> usize {
        self.constants.push(value);
        self.constants.len() - 1
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
