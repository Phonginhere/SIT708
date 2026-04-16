package com.example.pass51.data

object DummyData {

    val allNews: List<NewsItem> = listOf(
        // ---- Featured stories (Top Stories) ----
        NewsItem(
            id = 1,
            title = "Australia Wins the Ashes",
            description = "Australia has clinched the Ashes series with a dominant performance at the MCG. Pat Cummins led the bowling attack with 4 wickets, while Steve Smith's century proved the difference.",
            imageUrl = "img_cricket_1",
            category = "Cricket",
            isFeatured = true
        ),
        NewsItem(
            id = 2,
            title = "Lakers Edge Past Warriors",
            description = "In a thrilling overtime contest, the Los Angeles Lakers defeated the Golden State Warriors 124-121. LeBron James recorded a triple-double with 32 points, 11 rebounds, and 10 assists.",
            imageUrl = "img_basketball_1",
            category = "Basketball",
            isFeatured = true
        ),
        NewsItem(
            id = 3,
            title = "Manchester Derby Ends in Draw",
            description = "Manchester United and Manchester City played out a dramatic 2-2 draw at Old Trafford. Marcus Rashford's late equaliser in the 89th minute ensured a share of the points.",
            imageUrl = "img_football_1",
            category = "Football",
            isFeatured = true
        ),
        NewsItem(
            id = 4,
            title = "Djokovic Claims Record Title",
            description = "Novak Djokovic has won yet another Grand Slam title, defeating Carlos Alcaraz in a five-set thriller at the Australian Open, extending his record tally of major titles.",
            imageUrl = "img_tennis_1",
            category = "Tennis",
            isFeatured = true
        ),

        // ---- Latest news (non-featured) ----
        NewsItem(
            id = 5,
            title = "IPL Auction Sets New Spending Record",
            description = "The latest IPL mega auction has broken all spending records, with franchises collectively spending over 5 billion rupees. Young domestic players were in high demand.",
            imageUrl = "img_cricket_2",
            category = "Cricket"
        ),
        NewsItem(
            id = 6,
            title = "Premier League Title Race Heats Up",
            description = "With just 10 games remaining, Arsenal, Liverpool, and Manchester City are separated by just three points at the top of the table.",
            imageUrl = "img_football_2",
            category = "Football"
        ),
        NewsItem(
            id = 7,
            title = "NBA All-Star Weekend Highlights",
            description = "The NBA All-Star Weekend delivered spectacular entertainment with the slam dunk contest stealing the show. Anthony Edwards wowed the crowd with a perfect score.",
            imageUrl = "img_basketball_2",
            category = "Basketball"
        ),
        NewsItem(
            id = 8,
            title = "Socceroos Qualify for World Cup",
            description = "Australia's Socceroos have secured their spot at the next FIFA World Cup. A crucial 1-0 victory over Japan, courtesy of a late goal from Mathew Leckie, sealed qualification.",
            imageUrl = "img_football_3",
            category = "Football"
        ),
        NewsItem(
            id = 9,
            title = "Wimbledon Introduces Shot Clock",
            description = "The All England Club has announced a new 25-second shot clock for all matches starting next season, aiming to speed up play and improve the spectator experience.",
            imageUrl = "img_tennis_2",
            category = "Tennis"
        ),
        NewsItem(
            id = 10,
            title = "NBL Grand Final Goes to Game 5",
            description = "The NBL Grand Final series is heading to a decisive fifth game after Melbourne United forced a decider with a gutsy 98-93 win over the Perth Wildcats.",
            imageUrl = "img_basketball_3",
            category = "Basketball"
        ),
        NewsItem(
            id = 11,
            title = "India Tops ICC Test Rankings",
            description = "India has reclaimed the number one spot in the ICC Test rankings following their series sweep against South Africa, featuring centuries from Virat Kohli and Shubman Gill.",
            imageUrl = "img_cricket_3",
            category = "Cricket"
        ),
        NewsItem(
            id = 12,
            title = "Melbourne Cup Field Announced",
            description = "The field for this year's Melbourne Cup has been finalised, with 24 horses set to compete. International runners from Ireland and Japan are among the favourites.",
            imageUrl = "img_horse_racing",
            category = "Horse Racing"
        )
    )

    // Property — return only items where isFeatured is true
    val featuredStories: List<NewsItem>
        get() = allNews.filter { it.isFeatured }

    // Property — return only items where isFeatured is false
    val latestNews: List<NewsItem>
        get() = allNews.filter { !it.isFeatured }

    // Property — return a list starting with "All" followed by unique category names, sorted
    val categories: List<String>
        get() = listOf("All") + allNews.map { it.category }.distinct().sorted()

    // Function — if category is "All", return everything; otherwise filter by category
    fun filterByCategory(category: String): List<NewsItem> {
        return if (category == "All") allNews else allNews.filter { it.category == category }
    }

    // Function — return items with the same category but a different id, limit to 4
    fun getRelatedStories(currentItem: NewsItem): List<NewsItem> {
        return allNews.filter { it.category == currentItem.category && it.id != currentItem.id }.take(4)
    }

    // Function — find and return the item with the matching id, or null if not found
    fun getById(id: Int): NewsItem? {
        return allNews.find { it.id == id }
    }
}