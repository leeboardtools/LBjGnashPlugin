# TODO
- Add ability to filter on Account notes (want to be able to add say 'real-estate' as a note,
and then exclude accounts with notes that contain 'real-estate'.

# LeeboardTools jGnash Plugin Design Notes
## Summary Report
    Overall summary of the accounts. Major items are:
    - Current Net worth (Assets - Liabilities)
    - Current Income (12 months)
    - Current Expenses (12 months)
    - Year ago Net worth
    - Year ago Income
    - Year ago Expenses

How is the Summary Report any different from any other report?
It's just some predefined reports.

## Reports
Need a report definition.
    - Filtering: determines what transactions get included
        - Filters can be exclusive or inclusive (exclude items or include items)
        - Types of filters:
            - Date range
                - Specific Dates
                - Periods (Annual, Quarter, Month)
                - Previous m months
                - Previous w weeks
                - Previous d days
                - After date
                - Before date
            - Account type
            - Account code
            - Account names

    - Presentation: how the transactions are displayed.
        - Display transactions
        - Display subtotals
            - At what account levels?
                - Number of levels from deepest
        - Display securities
            - Security, amount, market value
            - Include cash
        - Display columns for time periods


Need an Update button or hook into jgnash.engine.message.MessageBus.


Think about this a little more. What are the main types of reports, presentation-wise?
    - Transaction: this report displays individual transactions, by account. 
        Features:
        - Date range
        - Accounts displayed
            - Optional groups:
                - Assets/liabilities
                - Income/expenses
        - Sub-totals Y/N
        - Editable?

    - Net worth: displays net worth, with optional multiple columns to compare time periods.
        - Column frequency: defines the time period for the columns.
        - Column date range: defines extent of time periods to display.
        - Accounts included/excluded
            - Assets/liabilities only
        - Sub-totals Y/N

    - Profit loss: displays income - expenses for a given time period, with optional multiple columns to
        compare time periods (spacing of time periods may differ from profit-loss time period)
        - Profit-loss time period: defines the time period over which to sum income/expenses.
        - Column frequency: defines the time period for the columns.
        - Column date range: defines extent of time periods to display.
        - Accounts included/excluded
            - Income/expenses only
        - Sub-totals Y/N

    - Securities Summary: Need better name, displays summary of securities, with optional multiple columns to
        compare time periods.
        - Column frequency: defines the time period for the columns.
        - Column date range: defines extent of time periods to display.
        - Accounts included/excluded
            - Investment type accounts.
        - Displays (all controllable):
            - Shares held
            - Market value
            - % of net worth
            - % of investments
            - short term/long term
            - taxable
            - Cost basis???
        - Sub-totals Y/N
            - total commissions/fees for account

OK, transactions report might not really be necessary, just a variation of a new transactions editor. For
now will skip that.

So we have the following reports:
    - Net Worth
    - Profit/Loss
    - Securities Summary reports

They all support:
    - Multiple columns, where the columns represent a time period.
        - Define the date range
        - Define the period spacing
    - Securities includes sub-columns.
    - Account filters
        - Have predefined filters for the different types, or rather the ability to only
            show specific account types, and then exclude/include individual accounts
            from those types.
    - Sub totaling options.
        - Depth at which to start sub-totaling
            - Depth is either from deepest or from root.
    
    - Presentation spacing:
    - Title


We have the following concepts:
    - Date range around a reference date (for profit/loss ranges)
        - Number of days
        - Number of months
        - Number of years
        - Number of quarters
        - Number of weeks

    - A starting date
        - Explicit date
        - Offset from the current date.
        - Previous year
        - Previous month
        - Previous quarter

    - A repeating period
        - Number of days
        - Number of weeks
        - Number of months
        - Number of years
        - Number of quarters

General algorithm:
    - Measurement date range for Profit/Loss, etc., single date for Net Worth
    - Starting date:
        - Based on DateOffset and LocalDate.now().
    - Repeating period
    - Number of periods
    Start with the starting date, obtained from the DateOffset.
        - Obtain the measurement date range.
        - while periods remaining
        - apply the period to the current date.

    - Column period: quarter


Example:
    - Profit/Loss for last 4 quarters:
        - Period: Periods.Standard.QUARTER, multiplier = -1
        - PeriodCount: 4
        - StartDateOffset: DateOffset.Standard.DAYS_FROM_END_OF_QUARTER, quarterOffset = -1, dayCount = 0
        - RangeGenerator: DateRange.Standard.CURRENT_QUARTER, quarterCount = 1
        - RangeOffset: null

    - Net worth for last 5 calendar years, per year:
        - Period: Periods.Standard.YEAR, multiplier = -1
        - PeriodCount: 4
        - StartDateOffset: DateOffset.Standard.DAYS_FROM_END_OF_YEAR, yearOffset = -1, dayCount = 0
        - RangeGenerator: null (not applicable)
        - RangeOffset: DateOffset.Standard.DAYS_FROM_END_OF_YEAR, yearOffset = 0, dayCount = 0

    - Net worth for last 5 fiscal years, fiscal year starts calendar Q2:
        - Period: Periods.Standard.YEAR, multiplier = -1
        - PeriodCount: 5
        - StartDateOffset: DateOffset.DAYS_FROM_END_OF_YEAR, yearOffset = -1, dayCount = 0
                DateOffset.DAYS_FROM_END_OF_QUARTER, quarterOffset = -2, dayCount = 0
        - RangeGenerator: null (not applicable)
        - RangeOffset: 


Some pre-defined ranges:
    - Previous N months. Falls on calendar boundaries
        - Every M months.
        - Include year to date? Y/N
    - Previous N calendar quarters. Falls on calendar boundaries
        - By month
        - By quarter
        - Include year to date? Y/N
    - Previous N calendar years. Falls on calendar boundaries
        - By month
        - By quarter
        - By year
        - Include year to date? Y/N
    - Last N 12 month periods.
        - By month.
        - By 3 month period.
        - By 12 month period.
        - Include year to date? Y/N


# Account Filtering:
    Account types   Account groups:
    ASSET           ASSET
    BANK            ASSET
    CASH            ASSET
    CHECKING        ASSET
    CREDIT          LIABILITY
    EQUITY          EQUITY
    EXPENSE         EXPENSE
    INCOME          INCOME
    INVEST          INVEST
    SIMPLEINVEST    SIMPLEINVEST
    LIABILITY       LIABILITY
    MONEYMKRT       ASSET
    MUTUAL          INVEST
    ROOT            ROOT




IMMEDIATE:
