module Main where

import Prelude
  ( Either(..)
  , String, (++), null
  , Show, show
  , IO, (>>), (>>=), putStr, putStrLn
  , FilePath
  , readFile
  )
import Control.Monad      ( forM_, mapM_, return )
import System.Environment ( getArgs )
import System.Exit        ( exitFailure )

import AbsInstant
import LexInstant   ( Token )
import ParInstant   ( pProgram, myLexer )
import PrintInstant ( Print )

type Err        = Either String
type ParseFun a = [Token] -> Err a

class JSONable a where
  toJSON :: a -> IO()

runFile :: (JSONable a) => ParseFun a -> FilePath -> IO ()
runFile p f = readFile f >>= run p

run :: (JSONable a) => ParseFun a -> String -> IO ()
run p s =
  case p ts of
    Left err -> do
      putStrLn "Parse Failed."
      putStrLn err
      exitFailure
    Right program -> do
      toJSON program
  where
  ts = myLexer s

main :: IO ()
main = do
  args <- getArgs
  case args of
    fs -> mapM_ (runFile pProgram) fs

instance JSONable Program where
  toJSON (Prog stmts) = do
    putStr "{\"stmts\":["
    case stmts of
      [] -> return ()
      [s] -> do
        toJSON s
      s:ss -> do
        toJSON s
        forM_ ss (\stmt -> do
          putStr ","
          toJSON stmt
          )
    putStrLn "]}"

instance JSONable Stmt where
  toJSON (SAss (Ident name) exp) = do
    putStr "{\"SAss\":{\"name\":"
    putStr (show name)
    putStr ",\"exp\":"
    toJSON exp
    putStr "}}"
  toJSON (SExp exp) = do
    putStr "{\"SExp\":{\"exp\":"
    toJSON exp
    putStr "}}"

instance JSONable Exp where
  toJSON (ExpLit value) = putStr ("{\"ExpLit\":{\"value\":" ++ show value ++ "}}")
  toJSON (ExpVar (Ident name)) = putStr ("{\"ExpVar\":{\"name\":" ++ show name ++ "}}")
  toJSON (ExpAdd exp1 exp2) = expOpToJSON exp1 exp2 "Add"
  toJSON (ExpSub exp1 exp2) = expOpToJSON exp1 exp2 "Sub"
  toJSON (ExpMul exp1 exp2) = expOpToJSON exp1 exp2 "Mul"
  toJSON (ExpDiv exp1 exp2) = expOpToJSON exp1 exp2 "Div"

expOpToJSON :: Exp -> Exp -> String -> IO()
expOpToJSON exp1 exp2 op = do
  putStr ("{\"ExpOp\":{\"op\":{\"" ++ op ++ "\":{}},\"exp1\":")
  toJSON exp1
  putStr ",\"exp2\":"
  toJSON exp2
  putStr ("}}")
