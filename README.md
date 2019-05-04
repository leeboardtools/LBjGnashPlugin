# LBjGnashPlugin
This is my jGnash (https://sourceforge.net/projects/jgnash/) plugin that adds features that are important to me.

The plugin currently adds customizable reports to jGnash via the LeeboardTools Reports menu.

**IMPORTANT:** This plugin is **Use-At-Your-Own-Risk!!!** It is your responsibility to verify that what's reported is in fact correct and what you expect. Testing primarily consists of my running these reports on my jGnash accounts and satisfying my expectations. Your accounts may be significantly different, which may or may not result in unexpected behavior.

# Installing the plugin
Download the JAR file from https://github.com/leeboardtools/LBjGnashPlugin/blob/master/dist/LBJGnashPlugin.jar to the plugins folder of your jGnash installation. Note that this has only been tested with jGnash 3.0.2.

# Reports
The reports are primarily designed for screen use, there is currently no printing support. There is exporting to CSV files so you can import the data into a spreadsheet and prettify them there for printing.

The reports let you select the date of the report, and optionally specify a number of periods over which to report. For example, you can display the last 5 years, every year, or every quarter.

There are four built-in reports:
- Net Worth Report: This displays Assets, Liabilities, and the net worth (net worth = assets - liabilities) as of the current date.
- Income - Expense Report: This dispays Income, Expenses, and the net income (net income = income - expenses) from the current date to a period of 1 year prior the current date.
- Portfolio: This displays all sorts of information about accounts that have securities as of the current date.
- Securities: This is similar to Portfolio except that it reports by security, not by account. A security common to multiple accounts is aggregated together and displayed as one entry.

You can customize any of the built-in reports and save the customization as a report for future use. This is done by opening the report, customizing it via Configure command under the report window's Options button, and then saving it via the Save Setup command, also under the report window's Options button.

You can then open a report with that configuration via the main LeeboardTools menu > Reports > Report Manager menu command.

The Report Manager also lets you edit, create and delete report configurations.

Report configurations are stored in the jGnash folder of your home folder.

Also available in the Options button of the report window is the Export command, which lets you save the report in CSV (Comma Separated Values) format, for importing into a spreadsheet.

## Portfolio/Securities Reports
The portfolio report was the driving force behind this plugin. 
The following are the columns supported:
- Quantity: The number of shares. For cash this is simply the cash value. This is only displayed for individual securities and appears blank for parent accounts.
- Price: The price of the security as of the column's date, as reported by jGnash. This is only displayed for individual securities and appears blank for parent accounts.
- Cost Basis: This is the total cost basis of the security. The cost-basis is calculated for each 'lot' of the security when the 'lot' is first added.
- Market Value: Quantity * Price.
- Gain: Market Value - Cost Basis.
- % Gain: (Market Value - Cost Basis) * 100 / Cost Basis.
- % of Portfolio: Market Value * 100 / Portfolio Market Value
- Annual %: An estimate of the equivalent annual rate of return of all the lots in the security. More on this below.
- Cash In: This is the total cash used to purchase the security, excluding any reinvested dividend purchases.
- Cash Out: This is the total proceeds from sales of the security.
- Net Gain: Market Value + Cash Out - Cash In
- % Cash-In Gain: (Market Value + Cash Out - Cash In) * 100 / Cash In
- Cash In Annual % Return: Similar to Annual % except the Market Value + Cash Out is used in place of the market value and Cash In replaces the cost basis. More on this below.


### Cash Treatment
Cash is treated as a security with a price of 1.0000. The quantity is the monetary amount of the cash.

**Note:** For the Securities report, if you set the CUSIP/ISIN to "Cash", no quotes, the security will be treated as the Cash security and lumped with that. 
This is handy for treating money funds as cash.

### Lots in Portfolio Reports
jGnash 3.0.2 does not support security lots. However, the portfolio reports support a mechanism for tracking lots. It's fairly limited, but serves my purposes. It works via the Memo field of a transaction.

When you have a Buy Shares transaction, you can assign a name to the lot represented by the transaction by entering 'LOT:' (single-quotes excluded!) followed by the name of the lot. The lot name should not contain ';' or 'LOT:'.

When you have a Sell Shares transaction, you can specify the lots of shares that were sold by entering 'LOT:' followed by the name of the lot, just as was done for the Buy Shares entry. To specify more than one lot, separate the entries with a ';'. This only supports selling the entire lot.

As an example, if you bought 10 shares of AAPL on October 5, 2016, 5 shares on December 27, 2016, and 3 shares on January 5, 2017, you could set the Memo of the transactions to:
- October 5, 2016 Memo: "LOT:2016-10-05"
- December 27, 2016 Memo: "LOT:2016-12-27"
- January 5, 2017 Memo: "LOT:2017-01-05"

where the text entered into the Memo field is what is enclosed in the double-quotes, and does not include the double-quotes.

Then, if you sold the shares from December 27, 2016 and January 5, 2017, in a single transaction, for that transaction you would enter the following: "LOT:2016-12-27; LOT:2017-01-05".

If you do not specify lots in a Sell Shares transaction, the oldest shares will be sold first.

### Annual % Return Column
The Annual % Return column provides an estimate of the annual rate of return of the security. This is a multi-step process.

For any given security lot, there is the market value of the lot, the cost basis of the lot, and the date of the cost basis. From this information compound annual growth rate (CAGR) is computed for the column's date:

    CAGR = (Market Value / Cost Basis) ^ (1/time) - 1
    
where time is the time from the column's date to the cost basis date, in decimal years (i.e. 1 1/2 years = 1.5 years).

From the CAGR the equivalent value of the security lot at 1 year before the column's date is computed:

    YearAgoValue = Market Value / (1 + CAGR)

This YearAgoValue is then summed for all the lots in the security.

The final Annual % value is computed simply from:

    Annual % = (Total Market Value - Total YearAgoValue) * 100 / Total YearAgoValue

The sub-totals for accounts is computed similarly by summing the total YearAgoValues and the total market values for each security in the account.

For cash, things are a little tricky. The problem here is determining whether a cash inflow should be treated as essentially a new cash lot, or if the inflow is income from the cash account. 

At the moment a fairly simple approach is used. If any of the debit accounts in the transaction is from an account belonging to the Income account group, the transaction is treated as income from the cash account.

When the cash transaction is treated as income from the cash account, it is distributed among all the current cash lots according to the size of the lot.

### Cash-In/Cash-Out Treatment
Note that this is still under development and is a bit crude.

The cash-in/cash-out tracks the total inflows and outflows of cash, rather than individual lots.

The idea behind cash-in/cash-out tracking is to be able to track the overall growth of the cash put into an investment,
counting reinvested dividends and interest as growth of the original investment. This differs from the cost basis
tracking because in the cost basis tracking reinvested dividends are treated as new investments.

Cash-in/cash-out also tracks partial sales followed by repurchases. This is because sales add to the cash-out,
while the repurchases are added to the cash-in.

When a security is purchased that is not part of a reinvested dividend transaction (cash-in transactions are 
basically any Buy transaction whose memo field does not contain 'reinvested'), the amount
purchased and the date of the purchase are recorded, with the cash-in increasing by the purchase amount.

When a security is sold, the sold amount is added to the cash-out. The cash-in record is not modified.

If after a transaction the balance of the investment account is 0, the cash-in/cash-out tracking is reset. 
Both totals are zeroed, and the list of tracked cash-ins is cleared.

The Net Gain and % Cash-In Gain columns use a current value of Market Value + Total Cash Out, and a cost basis of Total Cash In.

The Cash-In Annual % Return column uses a computation similar to the Annual % Return column, except that instead of lots the recorded cash-in amount/dates are
used. The market value assigned to a particular cash-in entry is simply:
    
    Market Value = (Total Market Value + Total Cash Out) * Cash In Amount / Total Cash In

and the cost basis is:

    Cost Basis = Cash In Amount

The YearAgoValue is computed and summed, and the Annual % value computed from that.

This is currently a fairly simple accounting of cash-in/cash-out, as it does not properly allocate interest and dividends to the cash-in amounts.


# Building the Plugin
The plugin was developed using Netbeans 11.0 (https://netbeans.org/downloads/), I recommend using it to build the JAR.
JDK 11 must be installed. I use OpenJDK 11 on Ubuntu 18.04.

The Netbeans project is set up to expect jGnash 3.0.2 to be installed in a folder named 'jgnash_install' 
within the same folder that contains the LBjGnashPlugin folder. If you have a different version of jGnash, 
or have it in a different location, you will need to edit the Libraries in the Netbeans project. 
Just include all the JARs in the lib folder of your jGnash installation.
