use std::fs;
use std::io::{stdin, stdout, Write};
use std::path::PathBuf;

use crate::error::Result;

pub mod chunk;
pub mod debug;
pub mod error;
pub mod value;
pub mod vm;

use crate::vm::VM;

pub fn repl() -> Result<()> {
    let mut vm = VM::new();

    loop {
        stdout().write_all(b"> ")?;
        let mut line = String::new();
        stdin().read_line(&mut line)?;
        interpret(&mut vm, &line)?;
    }
}

fn interpret(vm: &mut VM, line: &str) -> Result<()> {
    todo!()
}

pub fn run_file(file: PathBuf) -> Result<()> {
    let mut vm = VM::new();
    let contents = fs::read_to_string(file)?;

    interpret(&mut vm, &contents)?;

    Ok(())
}
