=begin
= ビルド方法
antを使用し、コマンドラインから、
 $ ant jars
と実行します。

bin/ディレクトリにクラスファイルとリソースファイルが出力され
それをもとに、dist/pandaclient.jarが生成されます。

= 選択的コンパイル除外
以下のソースはコンパイル環境によってコンパイル対象から除外される
ことがあります。

: src/org/montsuqi/client/SSLSocketCreator.java
  JSSE(Java Secure Socket Extension)がない場合
: src/org/montsuqi/util/J2SELogger.java
  Java2 1.4より前のバージョンの場合
: src/org/montsuqi/util/Log4JLogger.java
  Jakarta Log4Jが見つからない場合

=end
