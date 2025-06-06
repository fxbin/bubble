package cn.fxbin.bubble.plugin.satoken.filter;

import cn.dev33.satoken.context.model.SaRequest;
import cn.dev33.satoken.context.model.SaResponse;
import cn.dev33.satoken.context.model.SaStorage;
import cn.dev33.satoken.servlet.model.SaRequestForServlet;
import cn.dev33.satoken.servlet.model.SaResponseForServlet;
import cn.dev33.satoken.servlet.model.SaStorageForServlet;
import cn.fxbin.bubble.plugin.satoken.context.SaTokenContextForTtlStaff;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * SaTokenContextFilter
 *
 * Sa-Token 上下文过滤器（基于 TransmittableThreadLocal 存储上下文信息）
 *
 * 使用方式：在请求进入时，通过此过滤器将 HttpServletRequest/Response 转换为 Sa-Token 封装对象，
 * 并调用 SaTokenContextForThreadLocalStorage.setBox(...) 绑定到当前线程，
 * 请求结束后，调用 removeBox() 清理 TransmittableThreadLocal 变量，避免内存泄露。
 *
 * @author fxbin
 * @version v1.0
 * @since 2023/12/15 15:45
 */
@Component
@Order(Integer.MIN_VALUE)
@WebFilter(filterName = "SaTokenContextFilter", urlPatterns = "/*")
public class SaTokenContextFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // 如有需要，可在此处初始化资源
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {

        // 强制转换为 HttpServletRequest 和 HttpServletResponse
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // 将 Servlet 对象转换为 Sa-Token 所需的封装对象
        SaRequest saRequest = new SaRequestForServlet(httpRequest);
        SaResponse saResponse = new SaResponseForServlet(httpResponse);
        // 这里的 SaStorage 通常也是基于 HttpServletRequest 实现，主要用于在请求范围内存取数据
        SaStorage saStorage = new SaStorageForServlet(httpRequest);

        // 将封装后的 Box 存入 TransmittableThreadLocal 中
        SaTokenContextForTtlStaff.setModelBox(saRequest, saResponse, saStorage);

        try {
            // 继续执行过滤链
            filterChain.doFilter(request, response);
        } finally {
            // 请求处理完成后，清除 ThreadLocal 中的上下文信息，防止内存泄露
            SaTokenContextForTtlStaff.clearModelBox();
        }
    }

    @Override
    public void destroy() {
        // 如有需要，可在此处释放资源
    }
}