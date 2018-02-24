# TODO
- Add ability to filter on Account notes (want to be able to add say 'real-estate' as a note,
and then exclude accounts with notes that contain 'real-estate'.


# Streaming:
Just store individual reports in the main jGnash folder.
File extension lbjgnashreport
Menu will:
    - list available reports.
    - New report... -> New File Chooser -> Report Editor.
    - Edit report... -> Open File Chooser -> Report Editor.
    - Delete report... -> Open File Chooser -> Confirm Delete.
    - Or just Manage Reports...
        Dialog box,
            displays list of existing reports.
            buttons for Open, New, Edit, Delete.


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
        - Display delta between columns
        - Display delta from first column


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
    - Multiple columns, where the columns represent a measurement at a particular time.
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
    - Delta values as well as absolute values.
    
    - Presentation spacing:
    - Title


PeriodicDateGenerator:
    - StartDateOffset: Defines the first date generated.
    - PeriodDateOffset: Defines the next date generated, relative to the current period's date.
    - PeriodCount or EndDateOffset: Defines how many dates are to be generated.

    Problem:
        - Want to be able to do say 'every month' from the first date.
        - Problem is that StartDateOffset will give the first date, but
            then a Basic(Interval.MONTH, IntervalRelation) will give either the first or
            last day of the month. Want to be able to give it relative to the current
            date. Could do that with a SubInterval offset, but how would that be adjusted
            to the original reference date?
            Maybe change IntervalRelation to IntervalReference, and have FIRST_DAY, CURRENT_DAY, LAST_DAY?
            FIRST_DAY and LAST_DAY as before, but CURRENT_DAY would use the appropriate day from the
            refDate to the interval.
            How would this work for years? Would really like it to adjust by dom/month.
            Quarters: Adjust by dom/month offset.
            Months: Adjust by dom/month.
            In all this, though, would like to apply the period consistently to the first date,
            so would really like to be able to clone the date offset while increasing the
            offset.

So the PeriodicDateGenerator would have:
- Start Date Editing:
    - Specific Date -> Interval = Day, offset interval is count of date from LocalDate.now().
    - Start of [Interval] -> Interval = Interval, offset interval is 0, IntervalRelation is FIRST_DAY
    - [Start/End] of previous [Interval] -> Interval = Interval, offset is -1/1, IntervalRelation is FIRST_DAY/LAST_DAY
    - [n] [Interval] ago -> Interval = Interval, offset is n, IntervalRelation is LAST_DAY.
- Repeat every:
    - [n] [Interval]
- Repeat:
    - [n] times
    - Until [date]

IMMEDIATE:
