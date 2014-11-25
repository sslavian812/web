<style>
ul {
    list-style-type: none;
    margin: 0;
    padding: 0;
}

li {
    display: inline;
}
</style>

<div class="header">

    <ul>
        <li><a href="/">Home</a></li>
        <li><a href="/personal">My words</a></li>
        <li><a href="/translate">Translate</a></li>
        %if not signed_in:
            <li><a href="/sign_in">Sign in</a></li>
            <li><a href="/sign_up">Sign up</a></li>
        %else:
            <li><a href="/sign_out">Sign out</a></li>
        %endif
    </ul>
</div>