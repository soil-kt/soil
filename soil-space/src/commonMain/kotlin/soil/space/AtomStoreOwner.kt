package soil.space

interface AtomStoreOwner {
    // getOrCreate
    fun createStore(key: String?, scope: AtomScope?): AtomStore
}
