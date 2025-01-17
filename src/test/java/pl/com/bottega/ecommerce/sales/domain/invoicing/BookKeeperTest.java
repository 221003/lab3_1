package pl.com.bottega.ecommerce.sales.domain.invoicing;

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.ClientData;
import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.Id;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductData;
import pl.com.bottega.ecommerce.sharedkernel.Money;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class BookKeeperTest {

	@Mock
	private InvoiceFactory factory;

	@Mock
	private TaxPolicy taxPolicy;
	
	private BookKeeper keeper;
	private Tax tax;
    private InvoiceRequest request;
    private ClientData client;
    private RequestItem item;
    
    @BeforeEach
    void setUp() throws Exception {
    	keeper = new BookKeeper(factory);
        taxPolicy = mock(TaxPolicy.class);
    	tax = new Tax(Money.ZERO, "description");
    	client = new ClientData(Id.generate(),"client");
    	request = new InvoiceRequest(client);
    	item = new RequestItem(mock(ProductData.class), 0, Money.ZERO);
    }

    @Test
    void invoiceShouldReturnOneItem() {
    	
    	when(taxPolicy.calculateTax(any(), any())).thenReturn(tax);
    	when(factory.create(any())).thenReturn(new Invoice(Id.generate(), null));
    	
    	request.add(item);
    	
    	Invoice invoice = keeper.issuance(request, taxPolicy);
    	assertEquals(invoice.getItems().size(), 1);
    }
    
    @Test
    void calculateTaxForInvoiceWithTwoItemsShouldBeCalledTwoTimes() {
    	
    	when(taxPolicy.calculateTax(any(), any())).thenReturn(tax);
    	when(factory.create(any())).thenReturn(new Invoice(Id.generate(), null));
    
    	request.add(item);    	
    	request.add(item);
    
    	Invoice invoice = keeper.issuance(request, taxPolicy);
    	verify(taxPolicy, times(2)).calculateTax(any(), any());
    }

    @Test
    void invoiceShouldReturnZeroItems() {
    	when(factory.create(any())).thenReturn(new Invoice(Id.generate(), null));
    	
    	Invoice invoice = keeper.issuance(request, taxPolicy);
    	assertEquals(invoice.getItems().size(), 0);
    }
    
    @Test
    void getNetShouldReturnZeroForInvoiceWithZeroItems() {
    	when(factory.create(any())).thenReturn(new Invoice(Id.generate(), null));
    	
    	Invoice invoice = keeper.issuance(request, taxPolicy);
    	assertEquals(invoice.getNet(), Money.ZERO);
    }
    
    @Test
    void calculateTaxShouldNotBeCalled() {
    	when(factory.create(any())).thenReturn(new Invoice(Id.generate(), null));

    	Invoice invoice = keeper.issuance(request, taxPolicy);
    	verify(taxPolicy, times(0)).calculateTax(any(), any());
    }
    
    @Test
    void invoiceShouldReturnTwentyItems() {
    	when(taxPolicy.calculateTax(any(), any())).thenReturn(tax);
    	when(factory.create(any())).thenReturn(new Invoice(Id.generate(), null));
    	
    	for(int i = 0; i < 20; i++) {
    		request.add(item);
    	}
    	
    	Invoice invoice = keeper.issuance(request, taxPolicy);
    	assertEquals(invoice.getItems().size(), 20);
    }
}
