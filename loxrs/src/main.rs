use std::path::PathBuf;
use std::process;

use clap::Parser;
use clap_verbosity_flag::Verbosity;
use human_panic::setup_panic;

use loxrs::error::{Error, Result};
use loxrs::{repl, run_file};

fn main() -> Result<()> {
    setup_panic!();
    let args = Cli::parse();
    env_logger::Builder::new()
        .filter_level(args.verbose.log_level_filter())
        .init();

    if let Some(file) = args.file {
        let result = run_file(file);
        if let Err(err) = result {
            eprintln!("{}", err);
            match err {
                Error::CompileError => process::exit(65),
                Error::RuntimeError => process::exit(70),
                Error::IoError(_) => process::exit(74),
                _ => {}
            }
        }
    } else {
        repl()?;
    }

    Ok(())
}

#[derive(Debug, Parser)]
#[command(author, version, about, long_about = None)]
struct Cli {
    #[arg(short, long)]
    file: Option<PathBuf>,

    #[command(flatten)]
    verbose: Verbosity,
}
