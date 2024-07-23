use crate::chunk::Chunk;
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

    if offset > 0 && chunk.lines[offset] == chunk.lines[offset - 1] {
        print!("   | ");
    } else {
        print!("{:4} ", chunk.lines[offset]);
    }

    let instruction = chunk.get(offset);
    match instruction {
        0 => constant_instruction("OP_CONSTANT", chunk, offset),
        1 => simple_instruction("OP_RETURN", offset),
        _ => {
            println!("Unknown opcode {}", instruction);
            offset + 1
        }
    }
}

fn simple_instruction(name: &str, offset: usize) -> usize {
    println!("{}", name);
    offset + 1
}

fn constant_instruction(name: &str, chunk: &Chunk, offset: usize) -> usize {
    let constant = chunk.get(offset + 1);
    print!("{:-16} {:04} '", name, constant);
    print_value(chunk.constants.get(constant as usize));
    println!("'");
    offset + 2
}
