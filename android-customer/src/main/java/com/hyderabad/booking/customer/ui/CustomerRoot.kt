package com.hyderabad.booking.customer.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hyderabad.booking.common.AppContainer
import com.hyderabad.booking.common.auth.AuthViewModel
import com.hyderabad.booking.common.auth.LoginScreen
import com.hyderabad.booking.common.model.CreateOrderResponse
import com.hyderabad.booking.common.ui.BackStack
import com.hyderabad.booking.common.ui.rememberBackStack
import com.hyderabad.booking.common.util.vmFactory
import com.hyderabad.booking.customer.Screen

@Composable
fun CustomerRoot(container: AppContainer, startPayment: (CreateOrderResponse) -> Unit) {
    val loggedIn by container.tokenStore.loggedIn.collectAsStateWithLifecycle()

    if (!loggedIn) {
        val vm: AuthViewModel = viewModel(
            factory = vmFactory {
                AuthViewModel(
                    api = container.api,
                    tokenStore = container.tokenStore,
                    role = "CUSTOMER",
                    blockedRoles = setOf("PARTNER", "ADMIN"),
                    blockedMessage = "This number belongs to a business account. Please use the Hyderabad Booking Partner app."
                )
            }
        )
        LoginScreen(
            viewModel = vm,
            title = "Hyderabad Booking",
            subtitle = "Book salons and tutors near you"
        )
    } else {
        CustomerNav(container, startPayment)
    }
}

@Composable
private fun CustomerNav(container: AppContainer, startPayment: (CreateOrderResponse) -> Unit) {
    val stack = rememberBackStack<Screen>(Screen.Main())
    val api = container.api

    when (val screen = stack.current) {
        is Screen.Main -> MainScreen(container, stack, screen.tab)

        is Screen.Results -> ResultsScreen(
            api = api,
            category = screen.category,
            initialArea = screen.area,
            title = screen.title,
            onBack = stack::pop,
            onOpenPartner = { stack.push(Screen.PartnerDetail(it)) }
        )

        is Screen.PartnerDetail -> PartnerDetailScreen(
            api = api,
            partnerId = screen.partnerId,
            onBack = stack::pop,
            onBook = { serviceId -> stack.push(Screen.Book(screen.partnerId, serviceId)) }
        )

        is Screen.Book -> BookScreen(
            api = api,
            partnerId = screen.partnerId,
            serviceId = screen.serviceId,
            onBack = stack::pop,
            onCheckout = { ids -> stack.replace(Screen.Checkout(ArrayList(ids))) },
            onFreeBooked = { stack.replace(Screen.Success("Your free demo class is requested. The tutor will confirm it shortly.")) }
        )

        is Screen.Checkout -> CheckoutScreen(
            api = api,
            bookingIds = screen.bookingIds,
            startPayment = startPayment,
            onBack = stack::pop,
            onDone = { stack.reset(Screen.Success("Payment successful! Your booking is confirmed.")) },
            onCancelled = { stack.reset(Screen.Main(1)) }
        )

        is Screen.Success -> SuccessScreen(
            message = screen.message,
            onViewBookings = { stack.reset(Screen.Main(1)) },
            onHome = { stack.reset(Screen.Main(0)) }
        )

        is Screen.Review -> ReviewScreen(
            api = api,
            bookingId = screen.bookingId,
            businessName = screen.businessName,
            onBack = stack::pop,
            onDone = stack::pop
        )
    }
}

@Composable
private fun MainScreen(container: AppContainer, stack: BackStack<Screen>, tab: Int) {
    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = tab == 0,
                    onClick = { stack.replace(Screen.Main(0)) },
                    icon = { Icon(Icons.Filled.Home, contentDescription = null) },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = tab == 1,
                    onClick = { stack.replace(Screen.Main(1)) },
                    icon = { Icon(Icons.Filled.DateRange, contentDescription = null) },
                    label = { Text("Bookings") }
                )
                NavigationBarItem(
                    selected = tab == 2,
                    onClick = { stack.replace(Screen.Main(2)) },
                    icon = { Icon(Icons.Filled.Person, contentDescription = null) },
                    label = { Text("Profile") }
                )
            }
        }
    ) { padding ->
        val modifier = Modifier.padding(padding)
        when (tab) {
            0 -> HomeScreen(
                container = container,
                modifier = modifier,
                onSearch = { category, area, title -> stack.push(Screen.Results(category, area, title)) },
                onOpenPartner = { stack.push(Screen.PartnerDetail(it)) }
            )
            1 -> BookingsScreen(
                api = container.api,
                modifier = modifier,
                onPay = { id -> stack.push(Screen.Checkout(arrayListOf(id))) },
                onReview = { id, name -> stack.push(Screen.Review(id, name)) }
            )
            else -> ProfileScreen(container = container, modifier = modifier)
        }
    }
}
