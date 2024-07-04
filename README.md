# SXM_App
 Implementation of the Smart eXchange Market (SXM App) in the context of Information Systems Applications Development course in AUEB

 Smart eXchange Market is a mobile app which uses the power of AI to create safe ads with items that users are interested to buy or even trade!

 # How it works?
 
For our database, we use Google's Firebase which has a convenient organization at both saving and keep track of all our data. Android Studio in fact uses a Firebase library which helped us save all our data.

Each user creates their own account with username and password. At the Home page, they are able to see any ads that might interest them. All ads show some pictures of the item, a description of it and of course (not mandatory) a money price and other item categories that would be tradable with it. The ads are shown either at the homepage with a scrollview or with a Google Maps marker (bottom left button opens map).

When a user is interested to buy or trade, all they have to do is to make an ad of an item to trade or a money offer. If the owner of the item accepts the request, they must meet at the ad's locaton to trade.

# How AI works in SXM

It works for the safety of ads. Using Teachable Machine we created a Tensorflow Image Classification Model using over 2000 pictures for specific item classes. Before the user posts an ad they must press a button called Smart Check, which checks if all chosen pictures match the selected ad category. For example, if a user says they sell a car and they display an animal picture, then the Smart Check will show an error message to the user.

# Screenshots of the actual app

![logo](https://github.com/PapageorgiouMarios/SXM_App/assets/102749207/a19844f6-7875-4650-b837-6d3fb6f77f7d)
![home](https://github.com/PapageorgiouMarios/SXM_App/assets/102749207/b9b1c1b4-f4c5-417b-af49-00d7af790333)
![create_ad](https://github.com/PapageorgiouMarios/SXM_App/assets/102749207/1ec33727-1e2c-408e-901a-16e1cc078b8e)
![ai_usage](https://github.com/PapageorgiouMarios/SXM_App/assets/102749207/94d25313-bed0-4534-8403-828d73da5060)
![ad](https://github.com/PapageorgiouMarios/SXM_App/assets/102749207/bb9b0ae9-32c5-4164-a27d-d2b786a7271d)
![what_trade](https://github.com/PapageorgiouMarios/SXM_App/assets/102749207/bb116421-1aa2-40f1-ab56-2ad7e204e2a1)
![pick_image](https://github.com/PapageorgiouMarios/SXM_App/assets/102749207/32786f26-26a8-42ff-a9c6-63ccdbf1ef0e)
![my_appointments](https://github.com/PapageorgiouMarios/SXM_App/assets/102749207/a8151b9c-de7e-48d1-8271-5aa765c29a5a)
