import Data.List (partition)
import Data.Typeable

-- Is using type classes cheating? The book did say Haskell was the correct choice for this task.
-- But yeah just use classes and polymorphism, done, makes it easy to add new types and keeps methods for the same type all in one place.
class Polymorphs a where
  method1 :: a -> String
  method2 :: a -> String
    
instance Polymorphs Integer where
  method1 x = "Method 1 running on an integer :)"
  method2 x = "Method 2 running on an integer :)"

instance Polymorphs String where
  method1 x = "Method 1 running on a string :)"
  method2 x = "Method 2 running on a string :)"

main :: IO ()
main = putStrLn (method2 (2 :: Integer))