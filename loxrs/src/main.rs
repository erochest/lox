use clap::Parser;
use clap_verbosity_flag::Verbosity;
use human_panic::setup_panic;

use loxrs::chunk::{Chunk, OpCode};
use loxrs::error::Result;
use loxrs::vm::VM;

fn main() -> Result<()> {
    setup_panic!();
    let args = Cli::parse();
    env_logger::Builder::new()
        .filter_level(args.verbose.log_level_filter())
        .init();

    let mut chunk = Chunk::new();
    let constant = chunk.add_constant(1.2);
    chunk.write(OpCode::OpConstant as u8, 123);
    chunk.write(constant as u8, 123);

    chunk.write(OpCode::OpReturn as u8, 123);

    let mut vm = VM::new();
    vm.interpret(&chunk)?;

    Ok(())
}

#[derive(Debug, Parser)]
#[command(author, version, about, long_about = None)]
struct Cli {
    #[command(flatten)]
    verbose: Verbosity,
}
