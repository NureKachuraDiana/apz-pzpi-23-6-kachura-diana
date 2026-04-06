Запити до ШІ

Напиши науково-технічний опис патерна проєктування Strategy.

Поясни переваги та недоліки патерна Strategy за класифікацією GoF.

Наведи приклад реалізації патерна Strategy.

Приклад програмного коду

1  public interface Strategy {
2      int execute(int a, int b);
3  }
4
5  public class AddStrategy implements Strategy {
6      public int execute(int a, int b) {
7          return a + b;
8      }
9  }
10
11  public class MultiplyStrategy implements Strategy {
12      public int execute(int a, int b) {
13          return a * b;
14      }
15  }
16
17  public class Context {
18      private Strategy strategy;
19
20      public Context(Strategy strategy) {
21          this.strategy = strategy;
22      }
23
24      public void setStrategy(Strategy strategy) {
25          this.strategy = strategy;
26      }
27
28      public int performOperation(int a, int b) {
29          return strategy.execute(a, b);
30      }
31  }
32
33  public class Main {
34      public static void main(String[] args) {
35          Context context = new Context(new AddStrategy());
36          System.out.println(context.performOperation(2, 3));
37
38          context.setStrategy(new MultiplyStrategy());
39          System.out.println(context.performOperation(2, 3));
40      }
41  }
