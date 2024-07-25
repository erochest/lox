use std::convert::TryInto;

use crate::chunk::{Chunk, OpCode};
use crate::value::print_value;

pub fn dissassemble_chunk(chunk: &Chunk, name: &str) {
    println!("== {} ==", name);

    let mut offset = 0usize;
    while offset < chunk.code.len() {
        offset = dissassemble_instruction(chunk, offset);
    }
}

pub fn dissassemble_instruction(chunk: &Chunk, offset: usize) -> usize {
    print!("{:04} ", offset);

    if offset >= chunk.code.len() {
        println!("End of chunk");
        return offset;
    }

    if offset > 0 && chunk.lines[offset] == chunk.lines[offset - 1] {
        print!("   | ");
    } else {
        print!("{:4} ", chunk.lines[offset]);
    }

    let instruction = chunk.get(offset);
    let instruction: OpCode = instruction.try_into().unwrap();
    match instruction {
        OpCode::OpConstant => constant_instruction("OP_CONSTANT", chunk, offset),
        OpCode::OpAdd => simple_instruction("OP_ADD", offset),
        OpCode::OpSubtract => simple_instruction("OP_SUBTRACT", offset),
        OpCode::OpMultiply => simple_instruction("OP_MULTIPLY", offset),
        OpCode::OpDivide => simple_instruction("OP_DIVIDE", offset),
        OpCode::OpNegate => simple_instruction("OP_NEGATE", offset),
        OpCode::OpReturn => simple_instruction("OP_RETURN", offset),
    }
}

fn simple_instruction(name: &str, offset: usize) -> usize {
    println!("{}", name);
    offset + 1
}

fn constant_instruction(name: &str, chunk: &Chunk, offset: usize) -> usize {
    let constant = chunk.get(offset + 1);
    print!("{:-16} {:04} '", name, constant);
    print_value(chunk.constants[constant as usize]);
    println!("'");
    offset + 2
}
