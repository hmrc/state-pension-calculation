You can use the sandbox environment to [test this API](https://developer.service.hmrc.gov.uk/api-documentation/docs/testing). It does not support [stateful behaviour](https://developer.service.hmrc.gov.uk/api-documentation/docs/testing/stateful-behaviour).

Supported test scenarios:

<table>
    <thead>
        <tr>
            <th>Scenario</th>
            <th>Request</th>
            <th>Response</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td>Get calculation for user without notes or qualifying years.</td>
            <td>
<pre>
{
    "nino": "RN000133",
    "gender": "M",
    "checkBrick": "SMIJ ",
    "finalise": false
}</pre>
            </td>
            <td><code>201 (Created)</code></td>
        </tr>
        <tr>
            <td>Get calculation for user with some notes and qualifying years.</td>
            <td>
<pre>
{
    "nino": "RN000134",
    "gender": "M",
    "checkBrick": "GREM ",
    "finalise": true
}</pre>
            </td>
            <td><code>201 (Created)</code></td>
        </tr>
        <tr>
            <td>Return a <code>RETIREMENT_DATE_AFTER_DEATH_DATE</code> error code</td>
            <td>
<pre>
{
    "nino": "RN000135",
    "gender": "M",
    "checkBrick": "PATA ",
    "finalise": false
}</pre>
            </td>
            <td><code>403 (Forbidden)</code></td>
        </tr>
        <tr>
            <td>Return a <code>TOO_EARLY</code> error code</td>
            <td>
<pre>
{
    "nino": "RN000136",
    "gender": "F",
    "checkBrick": "SAMB ",
    "finalise": false
}</pre>
            </td>
            <td><code>403 (Forbidden)</code></td>
        </tr>
        <tr>
            <td>Return an <code>UNKNOWN_BUSINESS_ERROR</code> error code</td>
            <td>
<pre>
{
    "nino": "RN000137",
    "gender": "M",
    "checkBrick": "JEAM ",
    "finalise": false
}</pre>
            </td>
            <td><code>403 (Forbidden)</code></td>
        </tr>
        <tr>
            <td>Return a <code>NOT_FOUND_NINO</code> error code</td>
            <td>
<pre>
{
    "nino": "RN000138",
    "gender": "M",
    "checkBrick": "BLOJ ",
    "finalise": false
}</pre>
            </td>
            <td><code>404 (Non Found)</code></td>
        </tr>
        <tr>
            <td>Return a <code>NOT_FOUND_MATCH</code> error code</td>
            <td>
<pre>
{
    "nino": "RN000139",
    "gender": "F",
    "checkBrick": "DOEJ ",
    "finalise": false
}</pre>
            </td>
            <td><code>404 (Not Found)</code></td>
        </tr>
        <tr>
            <td>Return a <strong>single</strong> calculation error code</td>
            <td>
<pre>
{
    "nino": "RN000140",
    "gender": "M",
    "checkBrick": "BADM ",
    "finalise": false
}</pre>
            </td>
            <td><code>403 (Forbidden)</code></td>
        </tr>
        <tr>
            <td>Return <strong>multiple</strong> calcuation error codes</td>
            <td>
<pre>
{
    "nino": "RN000141",
    "gender": "F",
    "checkBrick": "BADF ",
    "finalise": true,
    "fryAmount": 123.45
}</pre>
            </td>
            <td><code>403 (Forbidden)</code></td>
        </tr>
        <tr>
            <td>Return an <code>INTERNAL_SERVER_ERROR</code> error code</td>
            <td>
<pre>
{
    "nino": "RN000142",
    "gender": "F",
    "checkBrick": "MINK ",
    "finalise": false
}</pre>
            </td>
            <td><code>500 (Internal Server Error)</code></td>
        </tr>
        <tr>
            <td>Return a <code>SERVER_ERROR</code> error code</td>
            <td>
<pre>
{
    "nino": "RN000143",
    "gender": "M",
    "checkBrick": "SMIJ ",
    "finalise": true
}</pre>
            </td>
            <td><code>503 (Service Unavailable)</code></td>
        </tr>
        <tr>
            <td>Return a <code>MESSAGE_THROTTLED_OUT</code> error code</td>
            <td>
<pre>
{
    "nino": "RN000144",
    "gender": "M",
    "checkBrick": "SMIJ ",
    "finalise": true
}</pre>
            </td>
            <td><code>429 (Too Many Requests)</code></td>
        </tr>
    </tbody>
</table>