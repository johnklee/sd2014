package unitest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(value = Suite.class)  
@SuiteClasses(value={FAST.class, FET.class})
public class TestAll {}
