# Stripe payment flow

1. Volanie POST /user/stripe/customer (User context)
```
curl https://api.stripe.com/v1/customers \
  -H "Authorization: Bearer sk_test_YOURKEY" \
  -d email="buyer@example.com"
```

Je nutne poslat viac udajov ktore pridu na API, (first name, last name, email, shipping address) su required pre business model. Nutne pouzit userId ako idempotency key.

Stripe vrati customerId vo format "cus_xxxxx", nutne ulozit v user agregate a publikovat domain event

2. Volanie POST /user/stripe/setup-intent (User context)
```
curl https://api.stripe.com/v1/setup_intents \
  -H "Authorization: Bearer sk_test_YOURKEY" \
  -d customer=cus_xxxxxxxxxxxxx \
  -d "payment_method_types[]"=card \
  -d usage=off_session
```

Je nutne iba userId, pri command handlingu agregat ma customerId.
Operacia je stateless, API sluzi ako proxy pre FE a vrati mu (client_secret, setupIntentId). Nic z tohto nie je nutne davat do agregatu, setup intenty su short lived.

3. Potvrzeni platobnej metody - deje sa iba na FE, kvoli handling payment detailov
```js
const stripe = Stripe('pk_test_YOURPUBLISHABLEKEY');

// User fills card form, then:
const { setupIntent, error } = await stripe.confirmCardSetup(
  'seti_xxxxxxxxxxxxx_secret_yyyyyyyyyy',  // client_secret from backend
  {
    payment_method: {
      card: cardElement,  // Stripe Elements card input
      billing_details: {
        name: 'John Doe'
      }
    }
  }
);
```

Priradi customerovi so setup intent testovaciu VISA kartu (pm_card_visa alias). Tento request vrati objekt s fieldom payment_method.

4. Priradenie platobnej metody - POST /user/stripe/payment-method (User context)
Uz ziadny externy API call. FE zasle v requeste payment_method, string vo formate "pm_xxxx", API posle command na agregat ktory si ulozi ID


5. Vytvorenie uctu predajcu - POST /user/stripe/seller-account (User context)
```
curl https://api.stripe.com/v1/accounts \
  -H "Authorization: Bearer sk_test_YOURKEY" \
  -d type=express \
  -d country=SK \
  -d email="seller@example.com" \
  -d "capabilities[card_payments][requested]"=true \
  -d "capabilities[transfers][requested]"=true
```

Akcia jednoducho vytvara stripe ucet na ktory je mozne z platformy posielat peniaze. Je to jednoducho iny typ uctu, vracia ID vo formate "acct_xxxx". V produkci musi FE este volat API na vygenerovanie onboarding linkov zaleziac od nasho webu. Tu staci v BE prevolat tento endpoint a ulozit acct_xxxx id k userId aby boli mozne transfery

6. Rezervacia prostriedkov kupcu - POST /order/reserve-funds (Shipping/Order context)
```
curl https://api.stripe.com/v1/payment_intents \
  -H "Authorization: Bearer sk_test_YOURKEY" \
  -d amount=5175 \
  -d currency=eur \
  -d customer=cus_xxxxxxxxxxxxx \
  -d payment_method=pm_xxxxxxxxxxxxx \
  -d confirm=true \
  -d off_session=true \
  -d "payment_method_types[]"=card
```

Request ma payment method ID. Je nutne pouzit order id ako idempotency key! Asi by bolo fajn pouzit transfer_group na tracing flow penazi, order id je dobry kandidat.

7. Transfer prostriedkov predajcovi po dokonceni objednavky - POST /order/complete (Shipping/Order context)
```
curl https://api.stripe.com/v1/transfers \
  -H "Authorization: Bearer sk_test_YOURKEY" \
  -d amount=4500 \
  -d currency=eur \
  -d destination=acct_xxxxxxxxxxxxx
```

Request ma account ktoremu su nakoniec pripisane peniaze. Tiez nutny idempotency key (order id). A taktiez transfer_group, good to have.
