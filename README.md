# blumo-trade-calculation

目標ポートフォリオに基づく買付注文計算APIです。起動ポートは 1111 です。

## テストとビルド

```bash
mvn test
mvn package
```

## 起動

```bash
mvn spring-boot:run
```

## API

`POST /users/{userId}/trades`

```json
{ "amount": 10000 }
```
