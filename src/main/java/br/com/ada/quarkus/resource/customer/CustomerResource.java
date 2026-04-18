package br.com.ada.quarkus.resource.customer;

import br.com.ada.quarkus.model.Customer;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.net.URI;
import java.util.List;

/**
 * Recurso responsável pelos endpoints de clientes.
 *
 * <p>Recebe as requisições HTTP relacionadas ao cadastro,
 * consulta e atualização de clientes.</p>
 */
@Path("/clientes")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class CustomerResource {

    @Inject
    CustomerServiceOld customerService;

    /**
     * Lista todos os clientes cadastrados.
     *
     * @return lista de clientes com dados públicos.
     */
    @GET
    public List<CustomerResponse> list() {
        return customerService.list().stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Busca um cliente pelo id.
     *
     * @param id identificador do cliente.
     * @return dados públicos do cliente encontrado.
     */
    @GET
    @Path("/{id}")
    public CustomerResponse findById(@PathParam("id") Long id) {
        return toResponse(customerService.findById(id));
    }

    /**
     * Cadastra um novo cliente.
     *
     * @param request dados do cliente.
     * @param uriInfo informações da URI da requisição.
     * @return resposta HTTP 201 com o cliente criado.
     */
    @POST
    @Transactional
    public Response create(@Valid CreateCustomerRequest request, @Context UriInfo uriInfo) {
        Customer customer = customerService.create(toCustomer(request));
        CustomerResponse response = toResponse(customer);

        URI location = uriInfo.getAbsolutePathBuilder()
                .path(response.id().toString())
                .build();

        return Response.created(location)
                .entity(response)
                .build();
    }

    /**
     * Atualiza os dados permitidos de um cliente.
     *
     * @param id identificador do cliente.
     * @param request novos dados do cliente.
     * @return dados públicos do cliente atualizado.
     */
    @PUT
    @Path("/{id}")
    @Transactional
    public CustomerResponse update(@PathParam("id") Long id,
                                   @Valid UpdateCustomerRequest request) {
        Customer updatedCustomer = customerService.update(
                id,
                request.name(),
                request.email(),
                request.password()
        );

        return toResponse(updatedCustomer);
    }

    /**
     * Converte a entidade Customer em CustomerResponse.
     *
     * @param customer entidade de cliente.
     * @return resposta com dados públicos do cliente.
     */
    private CustomerResponse toResponse(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getName(),
                customer.getEmail()
        );
    }

    /**
     * Converte o request de criação em entidade Customer.
     *
     * @param request dados recebidos no cadastro.
     * @return entidade Customer.
     */
    private Customer toCustomer(CreateCustomerRequest request) {
        return new Customer(
                null,
                request.name(),
                request.cpf(),
                request.email(),
                request.password()
        );
    }
}