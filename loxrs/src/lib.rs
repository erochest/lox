use std::cell::RefCell;
use std::fs;
use std::io::{stdin, stdout, Write};
use std::path::PathBuf;

use crate::error::Result;

pub mod chunk;
pub mod compiler;
pub mod debug;
pub mod error;
pub mod scanner;
pub mod value;
pub mod vm;

use crate::vm::VM;

pub fn repl() -> Result<()> {
    let repl = Repl::new();
    loop {
        stdout().write_all(b"> ")?;
        let mut line = String::new();
        stdin().read_line(&mut line)?;
        repl.interpret(&line)?;
    }
}

pub fn run_file(file: PathBuf) -> Result<()> {
    let mut vm = VM::new();
    let contents = fs::read_to_string(file)?;

    vm.interpret(&contents)?;

    Ok(())
}

struct Repl<'a> {
    vm: RefCell<VM<'a>>,
}

impl<'a> Repl<'a> {
    fn new() -> Self {
        Self {
            vm: RefCell::new(VM::new()),
        }
    }

    fn interpret(&'a self, source: &str) -> Result<()> {
        let mut vm = self.vm.borrow_mut();
        vm.interpret(source)
    }
}
